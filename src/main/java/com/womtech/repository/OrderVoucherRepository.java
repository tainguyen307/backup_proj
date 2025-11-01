package com.womtech.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.womtech.entity.Order;
import com.womtech.entity.OrderVoucher;
import com.womtech.entity.OrderVoucherID;

@Repository
public interface OrderVoucherRepository extends JpaRepository<OrderVoucher, OrderVoucherID> {
	List<OrderVoucher> findByOrder(Order Order);
}
