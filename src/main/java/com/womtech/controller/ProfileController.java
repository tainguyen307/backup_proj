package com.womtech.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.womtech.entity.Address;
import com.womtech.entity.Order;
import com.womtech.entity.User;
import com.womtech.service.AddressService;
import com.womtech.service.CloudinaryService;
import com.womtech.service.GhnService;
import com.womtech.service.OrderService;
import com.womtech.service.UserService;
import com.womtech.util.AuthUtils;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class ProfileController {

	private final UserService userService;
	private final AddressService addressService;
	private final OrderService orderService;
	private final AuthUtils authUtils;
	private final CloudinaryService cloudinaryService;
	private final GhnService ghnService;

	@GetMapping("/profile")
	public String showProfilePage(HttpSession session, Model model, Principal principal,
								  @RequestParam(defaultValue = "0") int page,
								  @RequestParam(defaultValue = "5") int size,
								  @RequestParam(required = false) Integer status) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();

		Address defaultAddress = addressService.findByUserAndIsDefaultTrue(user).orElse(new Address());
		List<Address> listAddress = addressService.findByUser(user);

		boolean isAdmin = user.getRole() != null && user.getRole().getRolename() != null
				&& user.getRole().getRolename().equalsIgnoreCase("ADMIN");
		
		Map<Integer, String> statuses = new LinkedHashMap<>();
	    statuses.put(-1, "Tất cả");
	    statuses.put(0, "Đã hủy");
	    statuses.put(1, "Chờ xác nhận");
	    statuses.put(2, "Đã xác nhận");
	    statuses.put(3, "Đang chuẩn bị");
	    statuses.put(4, "Đã đóng gói");
	    statuses.put(5, "Đang giao");
	    statuses.put(6, "Đã giao");
	    statuses.put(7, "Hoàn trả");
		
		Page<Order> orderPage;
		
	    if (status == null || status == -1) {
	        orderPage = orderService.findByUser(user, PageRequest.of(page, size, Sort.by("createAt").descending()));
	    } else {
	        orderPage = orderService.findByUserAndStatus(user, status, PageRequest.of(page, size, Sort.by("createAt").descending()));
	    }

		model.addAttribute("user", user);
		model.addAttribute("defaultAddress", defaultAddress);
		model.addAttribute("listAddress", listAddress);
		model.addAttribute("isAdmin", isAdmin);
	    model.addAttribute("listOrder", orderPage.getContent());
	    model.addAttribute("orderCount", orderService.findByUser(user).size());
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", orderPage.getTotalPages());
	    model.addAttribute("selectedStatus", status == null ? -1 : status);
	    model.addAttribute("statuses", statuses);

		return "user/profile";
	}

	@PostMapping("/update")
	public String updateProfile(HttpSession session, Principal principal, @RequestParam String email,
			@RequestParam String username) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();

		user.setEmail(email);
		user.setUsername(username);
		userService.save(user);

		return "redirect:/user/profile";
	}
	
	@PostMapping("/update-info")
	public String updateDefaultAddress(HttpSession session, Principal principal,
									   @ModelAttribute("defaultAddress") Address defaultAddress,
									   @ModelAttribute("user") User currentUser) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		if (!user.getUserID().equals(currentUser.getUserID()))
			throw new AccessDeniedException("Không có quyền truy cập");
		
		Optional<Address> addressDbOpt = addressService.findById(defaultAddress.getAddressID());
		if (addressDbOpt.isEmpty()) {
			defaultAddress.setAddressID(null);
			defaultAddress.setUser(user);
			defaultAddress.setCreateAt(LocalDateTime.now());
			defaultAddress.setUpdateAt(LocalDateTime.now());
			addressService.save(defaultAddress);
			addressService.setDefaultAddress(defaultAddress);
		} else {
			Address addressDb = addressDbOpt.get();
			if (!addressDb.getUser().equals(user))
				throw new AccessDeniedException("Không có quyền truy cập");
			addressDb.setFullname(defaultAddress.getFullname());
			addressDb.setPhone(defaultAddress.getPhone());
			addressDb.setStreet(defaultAddress.getStreet());
			addressDb.setWard(defaultAddress.getWard());
			addressDb.setDistrict(defaultAddress.getDistrict());
			addressDb.setCity(defaultAddress.getCity());
			addressDb.setUpdateAt(LocalDateTime.now());
		    addressService.save(addressDb);
		}
		
		// Đổi mail
		user.setEmail(currentUser.getEmail());
		userService.save(user);
		
		return "redirect:/user/profile";
	}
	

	@PostMapping("/add-address")
	public String addAddress(HttpSession session, Principal principal,
	                         @RequestParam String fullname,
	                         @RequestParam String phone,
	                         @RequestParam String street,
	                         @RequestParam String ward,
	                         @RequestParam String district,
	                         @RequestParam String city,
	                         @RequestParam String cityId,
	                         @RequestParam String districtId,
	                         @RequestParam String wardId) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();

	    Address address = Address.builder()
	            .user(user)
	            .fullname(fullname)
	            .phone(phone)
	            .street(street)
	            .ward(ward)
	            .wardId(wardId)
	            .district(district)
	            .districtId(districtId)
	            .city(city)
	            .cityId(cityId)
	            .createAt(LocalDateTime.now())
	            .updateAt(LocalDateTime.now())
	            .build();

	    addressService.save(address);

	    // Nếu chưa có địa chỉ mặc định thì set luôn
	    if (addressService.findByUserAndIsDefaultTrue(user).isEmpty())
	        addressService.setDefaultAddress(address);

	    return "redirect:/user/profile?tab=address";
	}

	@PostMapping("/update-address")
	public String updateAddress(HttpSession session, Principal principal,
	                            @RequestParam String addressID,
	                            @RequestParam String fullname,
	                            @RequestParam String phone,
	                            @RequestParam String street,
	                            @RequestParam String ward,
	                            @RequestParam String district,
	                            @RequestParam String city,
	                            @RequestParam String cityId,
	                            @RequestParam String districtId,
	                            @RequestParam String wardId) throws Exception {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();

	    Optional<Address> addressDbOpt = addressService.findById(addressID);

	    if (addressDbOpt.isEmpty()) {
	        throw new Exception("Không tìm thấy ID địa chỉ");
	    } else {
	        Address addressDb = addressDbOpt.get();
	        if (!addressDb.getUser().equals(user))
	            throw new AccessDeniedException("Không có quyền truy cập");

	        addressDb.setFullname(fullname);
	        addressDb.setPhone(phone);
	        addressDb.setStreet(street);
	        addressDb.setWard(ward);
	        addressDb.setWardId(wardId);
	        addressDb.setDistrict(district);
	        addressDb.setDistrictId(districtId);
	        addressDb.setCity(city);
	        addressDb.setCityId(cityId);
	        addressDb.setUpdateAt(LocalDateTime.now());

	        addressService.save(addressDb);
	    }
	    return "redirect:/user/profile?tab=address";
	}
	
	@PostMapping("/setdefault-address")
	public String setDefault(HttpSession session, Principal principal,
							 @RequestParam String addressID) throws Exception {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Optional<Address> addressDbOpt = addressService.findById(addressID);
		
		if (addressDbOpt.isEmpty()) {
			throw new Exception("Không tìm thấy ID địa chỉ");
		} else {
			Address addressDb = addressDbOpt.get();
			if (!addressDb.getUser().equals(user))
				throw new AccessDeniedException("Không có quyền truy cập");
			addressService.setDefaultAddress(addressDb);
		}
		
		return "redirect:/user/profile?tab=address";
	}
	
	@PostMapping("/delete-address")
	public String deleteAddress(HttpSession session, Principal principal,
							 @RequestParam String addressID) throws Exception {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
		
		Optional<Address> addressDbOpt = addressService.findById(addressID);
		
		if (addressDbOpt.isEmpty()) {
			throw new Exception("Không tìm thấy ID địa chỉ");
		} else {
			Address addressDb = addressDbOpt.get();
			if (!addressDb.getUser().equals(user))
				throw new AccessDeniedException("Không có quyền truy cập");
			
			addressService.deleteById(addressID);
			
			if (addressService.findByUserAndIsDefaultTrue(user).isEmpty()) {
				List<Address> listAddress = addressService.findByUser(user);
				if (!listAddress.isEmpty())
					addressService.setDefaultAddress(listAddress.get(0));
			}
		}
		
		return "redirect:/user/profile?tab=address";
	}

	@PostMapping("/change-password")
	public String changePassword(HttpSession session, Principal principal, @RequestParam String currentPassword,
			@RequestParam String newPassword, @RequestParam String confirmPassword) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();

		if (!newPassword.equals(confirmPassword)) {
			return "redirect:/user/profile?err=pwd_mismatch";
		}
		if (!com.womtech.util.PasswordUtil.matches(currentPassword, user.getPassword())) {
			return "redirect:/user/profile?err=pwd_wrong";
		}
		user.setPassword(com.womtech.util.PasswordUtil.encode(newPassword));
		userService.save(user);

		return "redirect:/user/profile?ok=pwd_changed";
	}
	
	@PostMapping("/avatar")
	public String uploadAvatar(HttpSession session, Principal principal,
							   @RequestParam("avatar") MultipartFile file,
							   RedirectAttributes redirectAttributes) {
		Optional<User> userOpt = authUtils.getCurrentUser(principal);
		if (userOpt.isEmpty()) {
			return "redirect:/auth/login";
		}
		User user = userOpt.get();
	    try {

	        // Xóa avatar cũ nếu có
	        if (user.getAvatar() != null) {
	            cloudinaryService.deleteImage(user.getAvatar());
	        }

	        // Upload ảnh mới
	        String avatarUrl = cloudinaryService.uploadAvatar(file);
	        user.setAvatar(avatarUrl);
	        userService.save(user);

	        redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", e.getMessage());
	    }

	    return "redirect:/user/profile";
	}

}
