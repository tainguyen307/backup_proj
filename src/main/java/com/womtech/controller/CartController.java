package com.womtech.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.womtech.entity.Cart;
import com.womtech.entity.Product;
import com.womtech.entity.User;
import com.womtech.service.CartService;
import com.womtech.service.ProductService;
import com.womtech.util.AuthUtils;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
	private final CartService cartService;
	private final ProductService productService;
	private final AuthUtils authUtils;
	
	@GetMapping({"", "/"})
	public String showCart(HttpSession session, Model model, Principal principal) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Cart cart = cartService.findByUser(user);
		BigDecimal totalPrice = cartService.totalPrice(cart);
		int totalQuantity = cartService.totalQuantity(cart);
		
		model.addAttribute("user", user);
		model.addAttribute("cart", cart);
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("totalQuantity", totalQuantity);
		
		return "/user/cart";
	}
	
	@PostMapping("/add")
	public String addCartItem(HttpSession session, Model model, Principal principal,
							  @RequestParam String productID,
							  @RequestParam int quantity,
							  RedirectAttributes redirect) throws Exception {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Optional<Product> productOpt = productService.getProductById(productID);
		if (productOpt.isEmpty()) {
			throw new Exception("Không tìm thấy sản phẩm");
		}
		cartService.addToCart(user, productOpt.get(), quantity);
		
		redirect.addAttribute("added", true);
		redirect.addAttribute("quantity", quantity);
		return "redirect:/product/" + productID;
	}
	
	@PostMapping("/remove")
	public String removeCartItem(HttpSession session, Model model, Principal principal,
								 @RequestParam String cartItemID) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		
		cartService.removeItem(cartItemID);
		
		return "redirect:/cart";
	}
	
	@PostMapping("/clear")
	public String clearCart(HttpSession session, Model model, Principal principal) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Cart cart = cartService.findByUser(user);
		cartService.clearCart(cart);
		
		return "redirect:/cart";
	}
	
	@PostMapping("/update")
	public String updateQuantityCartItem(HttpSession session, Model model, Principal principal,
								 @RequestParam String cartItemID,
								 @RequestParam int quantity
								 ) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		
		cartService.updateQuantity(cartItemID, quantity);
		
		return "redirect:/cart";
	}

	@GetMapping("/count")
	@ResponseBody
	public Map<String, Object> getCartCount(Principal principal) {
	    int count = 0;
	    
	    Optional<User> userOpt = authUtils.getCurrentUser(principal);
	    if (userOpt.isPresent()) {
	    	User user = userOpt.get();
		    count = cartService.totalQuantity(cartService.findByUser(user));
	    }
	    
	    Map<String, Object> response = new HashMap<>();
	    response.put("totalItems", count);
	    return response;
	}
}
