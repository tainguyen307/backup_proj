package com.womtech.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.womtech.entity.Cart;
import com.womtech.entity.CartVoucher;
import com.womtech.entity.CartVoucherID;

@Repository
public interface CartVoucherRepository extends JpaRepository<CartVoucher, CartVoucherID> {
	List<CartVoucher> findByCart(Cart cart);
	void deleteByCart(Cart cart);
}
