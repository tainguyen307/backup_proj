package com.womtech.service;

import java.util.List;
import java.util.Optional;

import com.womtech.entity.Cart;
import com.womtech.entity.CartItem;
import com.womtech.entity.Product;

public interface CartItemService extends BaseService<CartItem, String> {

	Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

	List<CartItem> findByCart(Cart cart);
}