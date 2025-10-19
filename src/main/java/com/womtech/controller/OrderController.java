package com.womtech.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.womtech.entity.Order;
import com.womtech.entity.User;
import com.womtech.service.OrderService;
import com.womtech.util.AuthUtils;
import com.womtech.util.OrderStatusHelper;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;
	private final AuthUtils authUtils;
	
	@GetMapping("/{id}")
	public String showOrder(HttpSession session, Model model, Principal principal,
							@PathVariable String id) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Optional<Order> orderOpt = orderService.findById(id);
		if (orderOpt.isEmpty()){
			model.addAttribute("error", "Không tìm thấy đơn hàng.");
            return "user/order-detail";
		}
		
		Order order = orderOpt.get();
		if (!order.getUser().equals(user)) {
			return "error/403";
		}
		
		orderService.totalPrice(order);
		int totalQuantity = orderService.totalQuantity(order);
		
	    String orderStatusLabel = OrderStatusHelper.getOrderStatusLabel(order.getStatus());
	    String orderStatusBadge = OrderStatusHelper.getOrderStatusBadgeClass(order.getStatus());
	    String paymentStatusLabel = OrderStatusHelper.getPaymentStatusLabel(order.getPaymentStatus());
	    String paymentStatusBadge = OrderStatusHelper.getPaymentBadgeClass(order.getPaymentStatus());
		
		model.addAttribute("order", order);
		model.addAttribute("totalQuantity", totalQuantity);
	    model.addAttribute("orderStatusLabel", orderStatusLabel);
	    model.addAttribute("orderStatusBadge", orderStatusBadge);
	    model.addAttribute("paymentStatusLabel", paymentStatusLabel);
	    model.addAttribute("paymentStatusBadge", paymentStatusBadge);
		return "user/order-detail";

	}
	
	@PostMapping("/cancel")
	public String cancelOrder(HttpSession session, Model model, Principal principal,
			   @RequestParam String orderID) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Optional<Order> orderOpt = orderService.findById(orderID);
		if (orderOpt.isEmpty()){
			model.addAttribute("error", "Không tìm thấy đơn hàng.");
            return "user/order-detail";
		}
		
		Order order = orderOpt.get();
		if (!order.getUser().equals(user)) {
			return "error/403";
		}
		
		orderService.cancelOrder(orderID);
		return "redirect:/order/" + orderID;
	}
	
	@PostMapping("/payment")
	public String paymentOrder(HttpSession session, Model model, Principal principal,
							   @RequestParam String orderID) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
//		User user = userOpt.get();
		return "error/403";
	}
}
