package com.womtech.service;

import java.math.BigDecimal;
import java.util.List;

import com.womtech.entity.Cart;
import com.womtech.entity.CartVoucher;
import com.womtech.entity.CartVoucherID;
import com.womtech.entity.Voucher;

public interface CartVoucherService extends BaseService<CartVoucher, CartVoucherID> {

	List<CartVoucher> findByCart(Cart cart);

	void addVoucherToCart(Cart cart, Voucher voucher);

	BigDecimal getTotalDiscountPrice(Cart cart);

	void applyVouchersToCart(Cart cart, BigDecimal totalPrice);

}
