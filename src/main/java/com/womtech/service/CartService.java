package com.womtech.service;

import java.math.BigDecimal;

import com.womtech.entity.Cart;
import com.womtech.entity.Product;
import com.womtech.entity.User;

public interface CartService extends BaseService<Cart, String> {

	void addToCart(User user, Product product, int quantity);

	Cart findByUser(User user);

	void updateQuantity(String cartItemID, int quantity);

	void clearCart(User user);

	void removeItem(String cartItemID);

	BigDecimal totalPrice(Cart cart);

	int totalQuantity(Cart cart);
}