package com.womtech.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.womtech.entity.Cart;
import com.womtech.entity.CartItem;
import com.womtech.entity.Product;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
	List<CartItem> findByCart(Cart cart);
	
	Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}