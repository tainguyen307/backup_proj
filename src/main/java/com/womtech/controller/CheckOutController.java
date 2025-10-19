package com.womtech.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.womtech.entity.Address;
import com.womtech.entity.Cart;
import com.womtech.entity.Order;
import com.womtech.entity.User;
import com.womtech.service.AddressService;
import com.womtech.service.CartService;
import com.womtech.service.OrderService;
import com.womtech.util.AuthUtils;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckOutController {
	private final CartService cartService;
	private final AddressService addressService;
	private final OrderService orderService;
	private final AuthUtils authUtils;
	
	@GetMapping("")
	public String showCheckoutPage(HttpSession session, Model model, Principal principal) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Cart cart = cartService.findByUser(user);
		if (cart.getItems().isEmpty())
			return "redirect:/cart";
		BigDecimal totalPrice = cartService.totalPrice(cart);
		
		Optional<Address> defaultAddressOpt = addressService.findByUserAndIsDefaultTrue(user);
		List<Address> addresses = addressService.findByUser(user);
		
		model.addAttribute("cart", cart);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("defaultAddress", defaultAddressOpt.get());
		model.addAttribute("addresses", addresses);
		return "/user/checkout";
	}
	
	@PostMapping("")
	public String reloadCheckoutPage(HttpSession session, Model model, Principal principal,
									 @RequestParam String voucherCode) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Cart cart = cartService.findByUser(user);
		if (cart.getItems().isEmpty())
			return "redirect:/cart";
		BigDecimal totalPrice = cartService.totalPrice(cart);
		
		Optional<Address> defaultAddressOpt = addressService.findByUserAndIsDefaultTrue(user);
		List<Address> addresses = addressService.findByUser(user);
		
		model.addAttribute("cart", cart);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("defaultAddress", defaultAddressOpt.get());
		model.addAttribute("addresses", addresses);
		
		// Giảm giá theo voucher
		
		return "/user/checkout";
	}
	
	@PostMapping("/confirm")
	public String processCheckout(HttpSession session, Model model, Principal principal,
								  @RequestParam("selectedAddressId") String addressID,
								  @RequestParam String payment_method,
								  @RequestParam String voucherCode) throws Exception {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Address address = addressService.findById(addressID).orElse(null);
		Order order = orderService.createOrder(user, address, payment_method, voucherCode);
		
		return "redirect:/order/" + order.getOrderID();
	}
}
