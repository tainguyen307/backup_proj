package com.womtech.service;

import java.util.List;

import com.womtech.entity.Cart;
import com.womtech.entity.Order;
import com.womtech.entity.OrderItem;

public interface OrderItemService extends BaseService<OrderItem, String> {
	boolean hasUserPurchasedProduct(String userId, String productId);

	List<OrderItem> findByOrder(Order order);

	void createItemsFromCart(Order order, Cart cart);
}