package com.womtech.service.impl;

import com.womtech.entity.*;
import com.womtech.enums.CommissionType;
import com.womtech.repository.*;
import com.womtech.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final CommissionSettingRepository settingRepository;
    private final CommissionRepository commissionRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final OrderItemRepository orderItemRepository;

    // ===== QUẢN LÝ COMMISSION SETTING =====
    @Override
    public CommissionSetting getGlobalSetting() {
        return settingRepository.findByType(CommissionType.GLOBAL)
                .orElseGet(() -> {
                    CommissionSetting global = CommissionSetting.builder()
                            .type(CommissionType.GLOBAL)
                            .rate(10.0)
                            .updatedAt(LocalDateTime.now())
                            .updatedBy("system")
                            .build();
                    return settingRepository.save(global);
                });
    }

    @Override
    public List<Category> getAllCategoriesWithSettings() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<CommissionSetting> getCategorySetting(String categoryId) {
        return settingRepository.findByCategoryId(categoryId);
    }

    @Override
    public Optional<CommissionSetting> getSubcategorySetting(String subcategoryId) {
        return settingRepository.findBySubcategoryId(subcategoryId);
    }

    @Override
    @Transactional
    public void saveGlobalSetting(Double rate, String username) {
        CommissionSetting setting = settingRepository.findByType(CommissionType.GLOBAL)
                .orElse(new CommissionSetting());

        setting.setType(CommissionType.GLOBAL);
        setting.setReferenceId(null);
        setting.setRate(rate);
        setting.setUpdatedBy(username);

        settingRepository.save(setting);
    }

    @Override
    @Transactional
    public void saveCategorySetting(String categoryId, Double rate, String username) {
        if (rate == null || rate <= 0) {
            settingRepository.findByCategoryId(categoryId)
                    .ifPresent(settingRepository::delete);
            return;
        }

        CommissionSetting setting = settingRepository.findByCategoryId(categoryId)
                .orElse(new CommissionSetting());

        setting.setType(CommissionType.CATEGORY);
        setting.setReferenceId(categoryId);
        setting.setRate(rate);
        setting.setUpdatedBy(username);
        settingRepository.save(setting);
    }

    @Override
    @Transactional
    public void saveSubcategorySetting(String subcategoryId, Double rate, String username) {
        if (rate == null || rate <= 0) {
            settingRepository.findBySubcategoryId(subcategoryId)
                    .ifPresent(settingRepository::delete);
            return;
        }

        CommissionSetting setting = settingRepository.findBySubcategoryId(subcategoryId)
                .orElse(new CommissionSetting());

        setting.setType(CommissionType.SUBCATEGORY);
        setting.setReferenceId(subcategoryId);
        setting.setRate(rate);
        setting.setUpdatedBy(username);
        settingRepository.save(setting);
    }

    @Override
    @Transactional
    public void saveAllSettings(Double globalRate,
                                List<String> categoryIds,
                                List<Double> categoryRates,
                                List<String> subcategoryIds,
                                List<Double> subcategoryRates,
                                String username) {
        if (globalRate != null) {
            saveGlobalSetting(globalRate, username);
        }

        if (categoryIds != null && categoryRates != null) {
            for (int i = 0; i < categoryIds.size(); i++) {
                if (i < categoryRates.size()) {
                    saveCategorySetting(categoryIds.get(i), categoryRates.get(i), username);
                }
            }
        }

        if (subcategoryIds != null && subcategoryRates != null) {
            for (int i = 0; i < subcategoryIds.size(); i++) {
                if (i < subcategoryRates.size()) {
                    saveSubcategorySetting(subcategoryIds.get(i), subcategoryRates.get(i), username);
                }
            }
        }
    }

    @Override
    @Transactional
    public void resetToDefault(String username) {
        settingRepository.deleteAll();
        saveGlobalSetting(10.0, username);
    }

    // ===== ÁP DỤNG COMMISSION KHI TẠO ORDER =====
    @Override
    public Double getCommissionRateForProduct(Product product) {
        Subcategory subcategory = product.getSubcategory();

        if (subcategory != null) {
            Optional<CommissionSetting> subSetting = settingRepository.findBySubcategoryId(subcategory.getSubcategoryID());
            if (subSetting.isPresent()) {
                return subSetting.get().getRate();
            }

            Category category = subcategory.getCategory();
            if (category != null) {
                Optional<CommissionSetting> catSetting = settingRepository.findByCategoryId(category.getCategoryID());
                if (catSetting.isPresent()) {
                    return catSetting.get().getRate();
                }
            }
        }

        return getGlobalSetting().getRate();
    }

    @Override
    @Transactional
    public Commission createCommissionForOrderItem(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        Double rate = getCommissionRateForProduct(product); // rate là Double, % sàn thu

        BigDecimal price = orderItem.getPrice(); // BigDecimal
        Integer quantity = orderItem.getQuantity() == null ? 0 : orderItem.getQuantity();

        // Tính subtotal = price * quantity
        BigDecimal subtotalBD = price.multiply(BigDecimal.valueOf(quantity));

        // amountBD = subtotal * rate / 100, làm tròn 2 chữ số thập phân
        BigDecimal amountBD = subtotalBD
                .multiply(BigDecimal.valueOf(rate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // netTotal = subtotal - amountBD  (số tiền sau khi đã trừ commission)
        BigDecimal netTotalBD = subtotalBD.subtract(amountBD).setScale(2, RoundingMode.HALF_UP);

        // Lưu Commission (nếu Commission.amount là Double, convert; nếu Commission.amount là BigDecimal, lưu trực tiếp)
        Commission commission = Commission.builder()
                .orderItem(orderItem)
                .rate(rate)
                .amount(amountBD.doubleValue()) // nếu field là Double
                .createAt(LocalDateTime.now())
                .vendor(product.getOwnerUser())  // 🔥 Gán vendor ở đây
                .build();

        Commission saved = commissionRepository.save(commission);

        // Cập nhật orderItem: ghi commissionAmount và netTotal rồi lưu orderItem
        orderItem.setCommissionAmount(amountBD); // require OrderItem có BigDecimal commissionAmount
        orderItem.setNetTotal(netTotalBD);       // require OrderItem có BigDecimal netTotal
        orderItemRepository.save(orderItem);

        return saved;
    }


    // ===== BÁO CÁO COMMISSION =====
    @Override
    public Double calculateTotalCommission(LocalDateTime startDate, LocalDateTime endDate) {
        Double total = commissionRepository.calculateTotalCommissionByDateRange(startDate, endDate);
        return total != null ? total : 0.0;
    }

    @Override
    public List<Commission> getCommissionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return commissionRepository.findCommissionsByDateRange(startDate, endDate);
    }

    @Override
    public Long countCommissions(LocalDateTime startDate, LocalDateTime endDate) {
        return (long) getCommissionsByDateRange(startDate, endDate).size();
    }
    
    @Override
    public List<Object[]> getCommissionReport(LocalDateTime start, LocalDateTime end) {
        return commissionRepository.getCommissionReport(start, end);
    }

    @Override
    public Double getTotalCommission(LocalDateTime start, LocalDateTime end) {
        return commissionRepository.getCommissionReport(start, end)
                .stream()
                .mapToDouble(r -> (Double) r[2])
                .sum();
    }
}
