package com.womtech.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.entity.Cart;
import com.womtech.entity.CartItem;
import com.womtech.entity.Order;
import com.womtech.entity.OrderItem;
import com.womtech.entity.Product;
import com.womtech.repository.CartItemRepository;
import com.womtech.repository.OrderItemRepository;
import com.womtech.service.OrderItemService;

@Service
@Transactional
public class OrderItemServiceImpl extends BaseServiceImpl<OrderItem, String> implements OrderItemService {
	@Autowired
	OrderItemRepository orderItemRepository;
	@Autowired
	CartItemRepository cartItemRepository;

	public OrderItemServiceImpl(JpaRepository<OrderItem, String> repo) {
		super(repo);
	}

	@Override
	public boolean hasUserPurchasedProduct(String userId, String productId) {
		return orderItemRepository.hasUserPurchasedProduct(userId, productId);
	}
	
	@Override
	public void createItemsFromCart(Order order, Cart cart) {
    	List<CartItem> cartItems = cartItemRepository.findByCart(cart);
    	
    	for (CartItem cartItem : cartItems) {
    		Product product = cartItem.getProduct();
    		BigDecimal price = (product.getDiscount_price() != null && product.getDiscount_price().compareTo(BigDecimal.ZERO) > 0)
    				? product.getDiscount_price()
    						: product.getPrice();
    		
    		OrderItem orderItem = OrderItem.builder()
    									.order(order)
    									.product(product)
    									.price(price)
    									.quantity(cartItem.getQuantity())
    									.build();
    		orderItemRepository.save(orderItem);
    	}
    }

	@Override
	@Transactional(readOnly = true)
	public List<OrderItem> findByOrder(Order order) {
		return orderItemRepository.findByOrder(order);
	}
}