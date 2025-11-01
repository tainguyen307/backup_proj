package com.womtech.service;

import java.math.BigDecimal;
import java.util.List;

import com.womtech.entity.Cart;
import com.womtech.entity.Order;
import com.womtech.entity.OrderVoucher;
import com.womtech.entity.OrderVoucherID;

public interface OrderVoucherService extends BaseService<OrderVoucher, OrderVoucherID> {

	void createOrderVoucherFromCart(Order order, Cart cart);

	List<OrderVoucher> findByOrder(Order Order);

	BigDecimal getTotalDiscountPrice(Order order);

}
