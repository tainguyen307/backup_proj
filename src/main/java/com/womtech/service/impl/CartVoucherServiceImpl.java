package com.womtech.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.entity.Cart;
import com.womtech.entity.CartVoucher;
import com.womtech.entity.CartVoucherID;
import com.womtech.entity.Voucher;
import com.womtech.repository.CartVoucherRepository;
import com.womtech.service.CartService;
import com.womtech.service.CartVoucherService;
import com.womtech.service.VoucherService;

@Service
@Transactional
public class CartVoucherServiceImpl extends BaseServiceImpl<CartVoucher, CartVoucherID> implements CartVoucherService {
	@Autowired
	CartVoucherRepository cartVoucherRepository;
	@Autowired
	CartService cartService;
	@Autowired
	VoucherService voucherService;
	
	public CartVoucherServiceImpl(CartVoucherRepository repo) {
		super(repo);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CartVoucher> findByCart(Cart cart) {
		return cartVoucherRepository.findByCart(cart);
	}
	
	@Override
	public void addVoucherToCart(Cart cart, Voucher voucher) {
		CartVoucher cartVoucher = CartVoucher.builder()
											 .cart(cart)
											 .voucher(voucher)
											 .build();
		cartVoucherRepository.save(cartVoucher);
	}
	
	@Override
	public void applyVouchersToCart(Cart cart, BigDecimal totalPrice) {
		List<CartVoucher> cartVouchers = findByCart(cart);
		
		for (CartVoucher cv : cartVouchers) {
			if (!voucherService.isValid(cv.getVoucher()))
				cartVoucherRepository.delete(cv);
			if (!voucherService.isUsable(cv.getVoucher(), cart.getUser()))
				cartVoucherRepository.delete(cv);
		}
		
		cartVouchers = findByCart(cart);
		
		List<Voucher> listVoucher = cartVouchers.stream().map(v -> v.getVoucher()).toList();
		
		// Discount Value cho non-global voucher
		for (CartVoucher cv : cartVouchers) {
			Voucher currentVoucher = cv.getVoucher();
			
			if (currentVoucher.getOwner() == null)
				continue;
			
			BigDecimal totalProductPrice = cartService.totalPriceByOwner(cart, currentVoucher.getOwner());
			BigDecimal discountPrice = voucherService.discountPrice(currentVoucher, listVoucher, totalProductPrice, totalPrice);
			cv.setDiscountValue(discountPrice);
			cartVoucherRepository.save(cv);
		}
		
		// Discount Value cho global voucher
		BigDecimal totalGlobalProductPrice = totalPrice.subtract(getTotalNonGlobalDiscountPrice(cartVouchers));
		
		for (CartVoucher cv : cartVouchers) {
			Voucher currentVoucher = cv.getVoucher();
			
			if (currentVoucher.getOwner() != null)
				continue;
			
			BigDecimal discountPrice = voucherService.discountPriceGlobal(currentVoucher, listVoucher, totalGlobalProductPrice, totalPrice);
			cv.setDiscountValue(discountPrice);
			cartVoucherRepository.save(cv);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public BigDecimal getTotalDiscountPrice(Cart cart) {
		List<CartVoucher> cartVouchers = findByCart(cart);
		
		BigDecimal totalDiscountPrice = BigDecimal.ZERO;
		
		for (CartVoucher cv : cartVouchers) {
			totalDiscountPrice = totalDiscountPrice.add(cv.getDiscountValue());
		}
		
		return totalDiscountPrice;
	}
	
	@Transactional(readOnly = true)
	private BigDecimal getTotalNonGlobalDiscountPrice(List<CartVoucher> cartVouchers) {
		BigDecimal totalDiscountPrice = BigDecimal.ZERO;
		
		for (CartVoucher cv : cartVouchers) {
			if (cv.getVoucher().getOwner() == null)
				continue;
			totalDiscountPrice = totalDiscountPrice.add(cv.getDiscountValue());
		}
		
		return totalDiscountPrice;
	}
}
