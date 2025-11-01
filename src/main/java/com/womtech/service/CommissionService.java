package com.womtech.service;

import com.womtech.entity.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommissionService {
    
    // ===== QUẢN LÝ COMMISSION SETTING =====
    
    /**
     * Lấy Global setting (hoặc tạo mới nếu chưa có)
     */
    CommissionSetting getGlobalSetting();
    
    /**
     * Lấy tất cả categories với settings của chúng
     */
    List<Category> getAllCategoriesWithSettings();
    
    /**
     * Lấy setting của một category (nếu có)
     */
    Optional<CommissionSetting> getCategorySetting(String categoryId);
    
    /**
     * Lấy setting của một subcategory (nếu có)
     */
    Optional<CommissionSetting> getSubcategorySetting(String subcategoryId);
    
    /**
     * Lưu hoặc cập nhật Global setting
     */
    void saveGlobalSetting(Double rate, String username);
    
    /**
     * Lưu hoặc cập nhật Category setting
     */
    void saveCategorySetting(String categoryId, Double rate, String username);
    
    /**
     * Lưu hoặc cập nhật Subcategory setting
     */
    void saveSubcategorySetting(String subcategoryId, Double rate, String username);
    
    /**
     * Lưu tất cả settings từ form
     */
    void saveAllSettings(Double globalRate, 
                        List<String> categoryIds, 
                        List<Double> categoryRates,
                        List<String> subcategoryIds, 
                        List<Double> subcategoryRates,
                        String username);
    
    /**
     * Reset về mặc định (Global = 10%)
     */
    void resetToDefault(String username);
    
    // ===== ÁP DỤNG COMMISSION KHI TẠO ORDER =====
    
    /**
     * Tính commission rate cho một product
     * Ưu tiên: Subcategory > Category > Global
     */
    Double getCommissionRateForProduct(Product product);
    
    /**
     * Tạo Commission record khi có OrderItem mới
     * @return Commission đã được tạo
     */
    Commission createCommissionForOrderItem(OrderItem orderItem);
    
    // ===== BÁO CÁO COMMISSION =====
    
    /**
     * Tính tổng commission trong khoảng thời gian
     */
    Double calculateTotalCommission(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Lấy danh sách commissions trong khoảng thời gian
     */
    List<Commission> getCommissionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Tính tổng số đơn hàng có commission
     */
    Long countCommissions(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Object[]> getCommissionReport(LocalDateTime start, LocalDateTime end);

    Double getTotalCommission(LocalDateTime start, LocalDateTime end);
}