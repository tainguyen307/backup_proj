package com.womtech.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.entity.Order;
import com.womtech.entity.OrderVoucher;
import com.womtech.entity.User;
import com.womtech.entity.Voucher;
import com.womtech.repository.OrderRepository;
import com.womtech.repository.VoucherRepository;
import com.womtech.service.VoucherService;

@Service
@Transactional
public class VoucherServiceImpl extends BaseServiceImpl<Voucher, String> implements VoucherService {
	@Autowired
	VoucherRepository voucherRepository;
	@Autowired
	OrderRepository orderRepository;

    public VoucherServiceImpl(JpaRepository<Voucher, String> repo, VoucherRepository voucherRepository) {
        super(repo);
        this.voucherRepository = voucherRepository;
    }

    @Override
    public Voucher create(Voucher voucher) {
        if (voucher.getCode() == null || voucher.getCode().isBlank()) {
            throw new IllegalArgumentException("Mã voucher không được để trống");
        }

        if (voucherRepository.findByCode(voucher.getCode()).isPresent()) {
            throw new IllegalStateException("Mã voucher đã tồn tại");
        }

        if (voucher.getStatus() == null) {
            voucher.setStatus(1);
        }

        if (voucher.getDiscount() == null || voucher.getDiscount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0%");
        }
        
        if (voucher.getExpire_date() == null) {
            throw new IllegalArgumentException("Vui lòng nhập ngày và giờ hết hạn voucher");
        }

        if (!voucher.getExpire_date().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hết hạn phải sau thời điểm hiện tại");
        }
        
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher update(Voucher voucher) {
        Voucher existing = voucherRepository.findById(voucher.getVoucherID())
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));
        
        if (voucher.getDiscount() == null || voucher.getDiscount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0%");
        }
        
        if (voucher.getExpire_date() == null) {
            throw new IllegalArgumentException("Vui lòng nhập ngày và giờ hết hạn voucher");
        }

        if (!voucher.getExpire_date().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hết hạn phải sau thời điểm hiện tại");
        }
        
        existing.setCode(voucher.getCode());
        existing.setDiscount(voucher.getDiscount());
        existing.setMin_price(voucher.getMin_price());
        existing.setExpire_date(voucher.getExpire_date());
        existing.setOwner(voucher.getOwner());
        existing.setStatus(voucher.getStatus());

        return voucherRepository.save(existing);
    }

    @Override
    public void delete(String voucherId) {
        if (!voucherRepository.existsById(voucherId)) {
            throw new IllegalArgumentException("Voucher không tồn tại");
        }
        voucherRepository.deleteById(voucherId);
    }

    @Override
    public Optional<Voucher> findByCode(String code) {
        return voucherRepository.findByCode(code);
    }

    @Override
    public Page<Voucher> search(String code, Integer status, String ownerId, Pageable pageable) {
        return voucherRepository.searchVouchers(code, status, ownerId, pageable);
    }

    @Override
    public List<Voucher> getAllActive() {
        return voucherRepository.findByStatus(1);
    }

    @Override
    public void enableVoucher(String voucherId) {
        voucherRepository.findById(voucherId).ifPresent(v -> {
            v.setStatus(1);
            voucherRepository.save(v);
        });
    }

    @Override
    public void disableVoucher(String voucherId) {
        voucherRepository.findById(voucherId).ifPresent(v -> {
            v.setStatus(0);
            voucherRepository.save(v);
        });
    }
    
    @Override
	public boolean isValid(Voucher voucher) {
    	if (voucher.getStatus() == 0)
    		return false;
    	if (voucher.getExpire_date().isBefore(LocalDateTime.now()))
    		return false;
    	return true;
    }
    
    @Override
	public boolean isApplicable(Voucher voucher, BigDecimal totalPrice) {
    	if (voucher.getMin_price().compareTo(totalPrice) == 1)
    		return false;
    	return true;
    }
    
    @Override
	public boolean isUsable(Voucher voucher, User user) {
    	String code = voucher.getCode();
    	List<Order> orders = orderRepository.findByUser(user);
    	for (Order order : orders) {
    		List<OrderVoucher> orderVouchers = order.getOrderVouchers();
    		for (OrderVoucher ov : orderVouchers) {
    			if (ov.getVoucher().getCode().equals(code))
    				return false;
    		}
    	}
    	return true;
    }
    
    @Override
	public BigDecimal discountPrice(Voucher currentVoucher, List<Voucher> listVouchers, BigDecimal totalProductPrice, BigDecimal totalPrice) {
        if (totalProductPrice == null || totalProductPrice.compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.ZERO;
        
        if (!isApplicable(currentVoucher, totalPrice))
        	return BigDecimal.ZERO;
        
        if (currentVoucher.getOwner() == null)
        	return BigDecimal.ZERO;
        
        BigDecimal total = totalProductPrice;
        
        for (Voucher voucher : listVouchers) {
        	if (voucher == currentVoucher)
        		break;
        	
        	if (voucher.getOwner() == null)
        		continue; // Có owner
        	
        	if (!voucher.getOwner().getUserID().equals(currentVoucher.getOwner().getUserID()))
        		continue; // Khác owner
        	
        	if (!isApplicable(voucher, totalPrice))
        		continue;
        	
        	if (voucher.getDiscount().compareTo(BigDecimal.valueOf(100)) >= 0)
        		total = BigDecimal.ZERO; // Lớn hơn hoặc bằng 100
        	
        	BigDecimal discountRate = voucher.getDiscount().divide(BigDecimal.valueOf(100)); // discount%
        	total = total.multiply(BigDecimal.ONE.subtract(discountRate)); // Nhân cho 1 - discount%
        }
        
        BigDecimal currentRate = currentVoucher.getDiscount().divide(BigDecimal.valueOf(100));
        return total.multiply(currentRate); // Nhân cho discount%
    }
    
	@Override
	public BigDecimal discountPriceGlobal(Voucher currentVoucher, List<Voucher> listVouchers, BigDecimal totalGlobalProductPrice, BigDecimal totalPrice) {
        if (totalGlobalProductPrice == null || totalGlobalProductPrice.compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.ZERO;
        
        if (!isApplicable(currentVoucher, totalPrice))
        	return BigDecimal.ZERO;
        
        if (currentVoucher.getOwner() != null)
        	return BigDecimal.ZERO;
        
        BigDecimal total = totalGlobalProductPrice;
        
        for (Voucher voucher : listVouchers) {
        	if (voucher == currentVoucher)
        		break;
        	
        	if (voucher.getOwner() != null)
        		continue; // Có owner
        	
        	if (!isApplicable(voucher, totalPrice))
        		continue;
        	
        	if (voucher.getDiscount().compareTo(BigDecimal.valueOf(100)) >= 0)
        		total = BigDecimal.ZERO; // Lớn hơn hoặc bằng 100
        	
        	BigDecimal discountRate = voucher.getDiscount().divide(BigDecimal.valueOf(100)); // discount%
        	total = total.multiply(BigDecimal.ONE.subtract(discountRate)); // Nhân cho 1 - discount%
        }
        
        BigDecimal currentRate = currentVoucher.getDiscount().divide(BigDecimal.valueOf(100));
        return total.multiply(currentRate); // Nhân cho discount%
    }
	
	@Scheduled(cron = "0 0 * * * ?") // mỗi giờ
    public void disableExpiredVouchers() {
        List<Voucher> activeVouchers = voucherRepository.findByStatus(1);
        for (Voucher v : activeVouchers) {
            if (v.getExpire_date().isBefore(LocalDateTime.now())) {
                v.setStatus(0);
                voucherRepository.save(v);
            }
        }
    }
}