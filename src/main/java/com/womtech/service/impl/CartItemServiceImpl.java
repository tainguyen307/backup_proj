package com.womtech.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.entity.Cart;
import com.womtech.entity.CartItem;
import com.womtech.entity.Product;
import com.womtech.repository.CartItemRepository;
import com.womtech.service.CartItemService;

@Service
@Transactional
public class CartItemServiceImpl extends BaseServiceImpl<CartItem, String> implements CartItemService {
	@Autowired
	CartItemRepository cartItemRepository;
	
	public CartItemServiceImpl(JpaRepository<CartItem, String> repo) {
		super(repo);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CartItem> findByCart(Cart cart) {
		return cartItemRepository.findByCart(cart);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CartItem> findByCartAndProduct(Cart cart, Product product) {
		return cartItemRepository.findByCartAndProduct(cart, product);
	}
}