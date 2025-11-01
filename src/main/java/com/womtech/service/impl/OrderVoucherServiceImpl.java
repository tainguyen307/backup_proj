package com.womtech.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.entity.Cart;
import com.womtech.entity.CartVoucher;
import com.womtech.entity.Order;
import com.womtech.entity.OrderVoucher;
import com.womtech.entity.OrderVoucherID;
import com.womtech.repository.OrderVoucherRepository;
import com.womtech.service.OrderVoucherService;
import com.womtech.service.VoucherService;

@Service
@Transactional
public class OrderVoucherServiceImpl extends BaseServiceImpl<OrderVoucher, OrderVoucherID> implements OrderVoucherService {
	@Autowired
	OrderVoucherRepository orderVoucherRepository;
	@Autowired
	VoucherService voucherService;
	
	public OrderVoucherServiceImpl(OrderVoucherRepository repo) {
		super(repo);
	}
	
	@Override
	public void createOrderVoucherFromCart(Order order, Cart cart) {
		List<CartVoucher> cartVouchers = cart.getCartVouchers();
		
		for (CartVoucher cv : cartVouchers) {
			if (!voucherService.isValid(cv.getVoucher()))
				continue;
			if (!voucherService.isUsable(cv.getVoucher(), cart.getUser()))
				continue;
			if (!voucherService.isApplicable(cv.getVoucher(), order.getTotalPrice()))
				continue;
			if (cv.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0)
				continue;
			
			OrderVoucher orderVoucher = OrderVoucher.builder()
													.order(order)
													.voucher(cv.getVoucher())
													.discountValue(cv.getDiscountValue())
													.build();
			orderVoucherRepository.save(orderVoucher);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderVoucher> findByOrder(Order Order) {
		return orderVoucherRepository.findByOrder(Order);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BigDecimal getTotalDiscountPrice(Order order) {
		List<OrderVoucher> orderVouchers = findByOrder(order);
		
		BigDecimal totalDiscountPrice = BigDecimal.ZERO;
		
		for (OrderVoucher ov : orderVouchers) {
			totalDiscountPrice = totalDiscountPrice.add(ov.getDiscountValue());
		}
		
		return totalDiscountPrice;
	}
}
