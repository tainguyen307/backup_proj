package com.womtech.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.womtech.entity.Cart;
import com.womtech.entity.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
	Optional<Cart> findByUser(User user);
}