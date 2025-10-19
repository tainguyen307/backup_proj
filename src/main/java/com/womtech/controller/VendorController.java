package com.womtech.controller;

import com.womtech.entity.*;
import com.womtech.service.*;
import com.womtech.util.OrderStatusHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vendor")
public class VendorController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private SubcategoryService subcategoryService;

	@Autowired
	private BrandService brandService;

	@Autowired
	private SpecificationService specificationService;

	@Autowired
	private CloudinaryService cloudinaryService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;
	
	@Autowired
	private VoucherService voucherService;
	
	@Autowired
    private PostService postService;

	// Helper method to get current user
	private User getCurrentUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}
		System.out.println("Current username: " + authentication.getName());

		String UserID = authentication.getName();
		return userService.findById(UserID).orElseThrow(() -> new RuntimeException("User not found"));

	}

	// Helper method to check if user owns the product
	private boolean isOwner(Product product, User user) {
		return product.getOwnerUser() != null && product.getOwnerUser().getUserID().equals(user.getUserID());
	}

	// Helper method to check if user owns the orders

	// ========== DASHBOARD ==========
	@GetMapping("/dashboard")
	public String vendorDashboard(Authentication authentication, Model model) {
		User currentUser = getCurrentUser(authentication);
		List<Product> myProducts = productService.getAllProducts().stream().filter(p -> isOwner(p, currentUser))
				.collect(Collectors.toList());

		// Calculate statistics
		long totalProducts = myProducts.size();
		long activeProducts = myProducts.stream().filter(p -> p.getStatus() == 1).count();
		long outOfStockProducts = myProducts.stream().filter(p -> p.getStatus() == 2).count();

		// Calculate total value
		BigDecimal totalValue = productService.calculateTotalValueByOwnerId(currentUser.getUserID());

		// Order statistics
		Long totalOrders = orderService.countOrdersByVendorId(currentUser.getUserID());
		Long pendingOrders = orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(),
				OrderStatusHelper.STATUS_PENDING);
		Long shippedOrders = orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(),
				OrderStatusHelper.STATUS_SHIPPED);
		Long deliveredOrders = orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(),
				OrderStatusHelper.STATUS_DELIVERED);

		// Get recent orders
		List<Order> recentOrders = orderService.getOrdersByVendorId(currentUser.getUserID()).stream().limit(5)
				.collect(Collectors.toList());

		model.addAttribute("totalProducts", totalProducts);
		model.addAttribute("activeProducts", activeProducts);
		model.addAttribute("outOfStockProducts", outOfStockProducts);
		model.addAttribute("totalValue", totalValue);
		model.addAttribute("recentProducts", myProducts.stream().limit(5).collect(Collectors.toList()));
		model.addAttribute("currentUser", currentUser);
		model.addAttribute("OrderStatusHelper", OrderStatusHelper.class);
		model.addAttribute("recentOrders", recentOrders);
		model.addAttribute("deliveredOrders", deliveredOrders);
		model.addAttribute("shippedOrders", shippedOrders);
		model.addAttribute("totalOrders", totalOrders);
		model.addAttribute("pendingOrders", pendingOrders);

		return "vendor/dashboard";
	}

	// ========== PRODUCT MANAGEMENT ==========
	@GetMapping("/products")
	public String listProducts(@RequestParam(required = false) String search,
			@RequestParam(required = false) String categoryID, @RequestParam(required = false) String subcategoryID,
			@RequestParam(required = false) String brandId, @RequestParam(required = false) Integer status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			Authentication authentication, Model model) {

		User currentUser = getCurrentUser(authentication);

		// Get only products owned by current vendor
		List<Product> allProducts = productService.getAllProducts().stream().filter(p -> isOwner(p, currentUser))
				.collect(Collectors.toList());
		System.out.println(currentUser.getUserID() + " có " + allProducts.size() + " sản phẩm");
		// Apply filters
		if (search != null && !search.trim().isEmpty()) {
			String searchLower = search.toLowerCase().trim();
			allProducts = allProducts.stream().filter(p -> p.getName().toLowerCase().contains(searchLower)
					|| p.getProductID().toLowerCase().contains(searchLower)).collect(Collectors.toList());
		}

		if (categoryID != null && !categoryID.trim().isEmpty()) {
			allProducts = allProducts.stream()
					.filter(p -> p.getSubcategory().getCategory().getCategoryID().equals(categoryID))
					.collect(Collectors.toList());
		}

		if (subcategoryID != null && !subcategoryID.trim().isEmpty()) {
			allProducts = allProducts.stream().filter(p -> p.getSubcategory().getSubcategoryID().equals(subcategoryID))
					.collect(Collectors.toList());
		}

		if (brandId != null && !brandId.trim().isEmpty()) {
			allProducts = allProducts.stream().filter(p -> p.getBrand().getBrandID().equals(brandId))
					.collect(Collectors.toList());
		}

		if (status != null) {
			allProducts = allProducts.stream().filter(p -> p.getStatus().equals(status)).collect(Collectors.toList());
		}

		// Sort by createAt descending
		allProducts = allProducts.stream().sorted((p1, p2) -> p2.getCreateAt().compareTo(p1.getCreateAt()))
				.collect(Collectors.toList());

		// Manual pagination
		int start = Math.min(page * size, allProducts.size());
		int end = Math.min(start + size, allProducts.size());
		List<Product> pagedProducts = allProducts.subList(start, end);

		// Create Page object
		Pageable pageable = PageRequest.of(page, size);
		org.springframework.data.domain.PageImpl<Product> pageImpl = new org.springframework.data.domain.PageImpl<>(
				pagedProducts, pageable, allProducts.size());

		model.addAttribute("products", pagedProducts);
		model.addAttribute("page", pageImpl);
		model.addAttribute("categories", categoryService.getActiveCategories());
		model.addAttribute("brands", brandService.getActiveBrands());

		if (categoryID != null && !categoryID.trim().isEmpty()) {
			model.addAttribute("subcategories", subcategoryService.getSubcategoriesByCategoryId(categoryID));
		}

		return "vendor/products";
	}

	@GetMapping("/products/new")
	public String newProductForm(Authentication authentication, Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("brands", brandService.getAllBrands());
		return "vendor/product-form";
	}

	@GetMapping("/products/edit/{id}")
	public String editProductForm(@PathVariable String id, Authentication authentication, Model model) {
		User currentUser = getCurrentUser(authentication);
		Product product = productService.getProductById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		// Check ownership
		if (!isOwner(product, currentUser)) {
			throw new RuntimeException("You don't have permission to edit this product");
		}

		model.addAttribute("product", product);
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("brands", brandService.getAllBrands());
		return "vendor/product-form";
	}

	@PostMapping("/products/save")
	public String saveProduct(@ModelAttribute Product product,
			@RequestParam(required = false) MultipartFile thumbnailFile, Authentication authentication,
			RedirectAttributes redirectAttributes) {

		try {
			User currentUser = getCurrentUser(authentication);

			// If editing, check ownership
			if (product.getProductID() != null && !product.getProductID().isEmpty()) {
				Product existingProduct = productService.getProductById(product.getProductID())
						.orElseThrow(() -> new RuntimeException("Product not found"));

				if (!isOwner(existingProduct, currentUser)) {
					throw new RuntimeException("You don't have permission to edit this product");
				}

				// Preserve existing thumbnail if no new file
				if (thumbnailFile == null || thumbnailFile.isEmpty()) {
					product.setThumbnail(existingProduct.getThumbnail());
				}
			}

			// Auto-assign current user as owner
			product.setOwnerUser(currentUser);

			// Handle thumbnail upload
			if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
				String imageUrl = cloudinaryService.uploadImage(thumbnailFile);
				product.setThumbnail(imageUrl);
			}

			productService.saveProduct(product);
			redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được lưu thành công!");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu sản phẩm: " + e.getMessage());
		}

		return "redirect:/vendor/products";
	}

	@GetMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable String id, Authentication authentication,
			RedirectAttributes redirectAttributes) {
		try {
			User currentUser = getCurrentUser(authentication);
			Product product = productService.getProductById(id)
					.orElseThrow(() -> new RuntimeException("Product not found"));

			// Check ownership
			if (!isOwner(product, currentUser)) {
				throw new RuntimeException("You don't have permission to delete this product");
			}

			// Delete thumbnail from Cloudinary if exists
			if (product.getThumbnail() != null && !product.getThumbnail().isEmpty()) {
				try {
					cloudinaryService.deleteImage(product.getThumbnail());
				} catch (Exception e) {
					// Log but continue with deletion
					System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
				}
			}

			productService.deleteProduct(id);
			redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được xóa thành công!");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa sản phẩm: " + e.getMessage());
		}
		return "redirect:/vendor/products";
	}

	// ========== SPECIFICATION MANAGEMENT ==========
	@GetMapping("/specifications/product/{productId}")
	public String listSpecifications(@PathVariable String productId, Authentication authentication, Model model) {
		User currentUser = getCurrentUser(authentication);
		Product product = productService.getProductById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		// Check ownership
		if (!isOwner(product, currentUser)) {
			throw new RuntimeException("You don't have permission to view specifications for this product");
		}

		List<Specification> specifications = specificationService.getSpecificationsByProduct(product);

		model.addAttribute("product", product);
		model.addAttribute("specifications", specifications);
		model.addAttribute("specCount", specifications.size());

		return "vendor/specifications-by-product";
	}

	@GetMapping("/specifications/new/{productID}")
	public String newSpecificationFormForProduct(@PathVariable String productID, Model model) {
		Product product = productService.getProductById(productID)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		Specification specification = new Specification();
		specification.setProduct(product);

		model.addAttribute("specification", specification);
		model.addAttribute("product", product);

		return "vendor/specification-form";
	}

	@GetMapping("/specifications/edit/{id}")
	public String editSpecificationForm(@PathVariable String id, Authentication authentication, Model model) {
		User currentUser = getCurrentUser(authentication);
		Specification specification = specificationService.getSpecificationByID(id)
				.orElseThrow(() -> new RuntimeException("Specification not found"));

		// Check ownership through product
		if (!isOwner(specification.getProduct(), currentUser)) {
			throw new RuntimeException("You don't have permission to edit this specification");
		}

		model.addAttribute("specification", specification);
		model.addAttribute("product", specification.getProduct());

		return "vendor/specification-form";
	}

	@PostMapping("/specifications/save")
	public String saveSpecification(@ModelAttribute Specification specification,
			@RequestParam(required = false) String productID, RedirectAttributes redirectAttributes) {
		try {
			// Nếu có productID từ form, set product
			if (productID != null && !productID.isEmpty()) {
				Product product = productService.getProductById(productID)
						.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
				specification.setProduct(product);
			}

			specificationService.saveSpecification(specification);
			redirectAttributes.addFlashAttribute("success", "Thông số kỹ thuật đã được lưu thành công!");

			// Redirect về trang specifications của product nếu có
			if (specification.getProduct() != null) {
				return "redirect:/vendor/specifications/product/" + specification.getProduct().getProductID();
			}

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu thông số: " + e.getMessage());
		}

		return "redirect:/vendor/specifications";
	}

	@GetMapping("/specifications/delete/{id}")
	public String deleteSpecification(@PathVariable String id, Authentication authentication,
			RedirectAttributes redirectAttributes) {
		try {
			User currentUser = getCurrentUser(authentication);
			Specification specification = specificationService.getSpecificationByID(id)
					.orElseThrow(() -> new RuntimeException("Specification not found"));

			String productId = specification.getProduct().getProductID();

			// Check ownership
			if (!isOwner(specification.getProduct(), currentUser)) {
				throw new RuntimeException("You don't have permission to delete this specification");
			}

			specificationService.deleteSpecification(id);
			redirectAttributes.addFlashAttribute("success", "Thông số kỹ thuật đã được xóa thành công!");

			return "redirect:/vendor/specifications/product/" + productId;

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa thông số: " + e.getMessage());
			return "redirect:/vendor/products";
		}
	}

	// ========== ORDER MANAGEMENT ==========
	@GetMapping("/orders")
	public String listOrders(@RequestParam(required = false) Integer status,
			@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, Authentication authentication, Model model) {

		User currentUser = getCurrentUser(authentication);

		// Get orders that contain vendor's products
		List<Order> allOrders;
		if (status != null) {
			allOrders = orderService.getOrdersByVendorIdAndStatus(currentUser.getUserID(), status);
		} else {
			allOrders = orderService.getOrdersByVendorId(currentUser.getUserID());
		}
		if (status != null) {
			allOrders = orderService.getOrdersByVendorIdAndStatus(currentUser.getUserID(), status);
			System.out.println("Số lượng đơn hàng với trạng thái " + status + ": " + allOrders.size());
		} else {
			allOrders = orderService.getOrdersByVendorId(currentUser.getUserID());
			System.out.println("Số lượng đơn hàng tổng: " + allOrders.size());
		}

		// Apply search filter by orderID, username, or shippingPhone
		if (search != null && !search.trim().isEmpty()) {
			String searchLower = search.toLowerCase().trim();
			allOrders = allOrders.stream()
					.filter(o -> o.getOrderID().toLowerCase().contains(searchLower)
							|| (o.getUser() != null && o.getUser().getUsername() != null
									&& o.getUser().getUsername().toLowerCase().contains(searchLower))
							|| (o.getAddress().getPhone() != null
									&& o.getAddress().getPhone().toLowerCase().contains(searchLower)))
					.collect(Collectors.toList());
			System.out.println("Số lượng đơn hàng sau tìm kiếm: " + allOrders.size());
		}

		// Manual pagination
		int start = Math.min(page * size, allOrders.size());
		int end = Math.min(start + size, allOrders.size());
		List<Order> pagedOrders = allOrders.subList(start, end);

		Pageable pageable = PageRequest.of(page, size);
		org.springframework.data.domain.PageImpl<Order> pageImpl = new org.springframework.data.domain.PageImpl<>(
				pagedOrders, pageable, allOrders.size());

		// Count by status
		Map<String, Long> statusCounts = new java.util.HashMap<>();
		statusCounts.put("ALL", orderService.countOrdersByVendorId(currentUser.getUserID()));
		statusCounts.put("PENDING",
				orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(), OrderStatusHelper.STATUS_PENDING));
		statusCounts.put("CONFIRMED", orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(),
				OrderStatusHelper.STATUS_CONFIRMED));
		statusCounts.put("PREPARING", orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(),
				OrderStatusHelper.STATUS_PREPARING));
		statusCounts.put("PACKED",
				orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(), OrderStatusHelper.STATUS_PACKED));
		statusCounts.put("SHIPPED",
				orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(), OrderStatusHelper.STATUS_SHIPPED));
		statusCounts.put("DELIVERED", orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(),
				OrderStatusHelper.STATUS_DELIVERED));
		statusCounts.put("CANCELLED", orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(),
				OrderStatusHelper.STATUS_CANCELLED));
		statusCounts.put("RETURNED", orderService.countOrdersByVendorIdAndStatus(currentUser.getUserID(),
				OrderStatusHelper.STATUS_RETURNED));

		model.addAttribute("orders", pagedOrders);
		model.addAttribute("page", pageImpl);
		model.addAttribute("statusCounts", statusCounts);
		model.addAttribute("currentStatus", status != null ? OrderStatusHelper.getOrderStatusLabel(status) : "ALL");
		model.addAttribute("OrderStatusHelper", OrderStatusHelper.class);

		return "vendor/orders";
	}

	@GetMapping("/orders/{id}")
	public String viewOrderDetail(@PathVariable String id, Authentication authentication, Model model) {
		User currentUser = getCurrentUser(authentication);

		Order order = orderService.getOrderById(id).orElseThrow(() -> new RuntimeException("Order not found"));

		// Get only items from this vendor
		List<OrderItem> vendorItems = orderService.getOrderItemsByOrderIdAndVendorId(id, currentUser.getUserID());

		if (vendorItems.isEmpty()) {
			throw new RuntimeException("You don't have permission to view this order");
		}

		// Calculate vendor's subtotal
		BigDecimal vendorSubtotal = vendorItems.stream()
				.map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		model.addAttribute("order", order);
		model.addAttribute("vendorItems", vendorItems);
		model.addAttribute("vendorSubtotal", vendorSubtotal);
		model.addAttribute("OrderStatusHelper", OrderStatusHelper.class);
		model.addAttribute("currentUser", currentUser);

		return "vendor/order-detail";
	}
	
	@PostMapping("/orders/cancel")
	public String cancelOrder(@RequestParam String orderId, Authentication authentication,
			RedirectAttributes redirectAttributes) {

		try {
			User currentUser = getCurrentUser(authentication);

			// Cancel only vendor's items in the order
			orderService.cancelVendorOrderItems(orderId, currentUser.getUserID());
			redirectAttributes.addFlashAttribute("success", "Đã hủy sản phẩm của bạn trong đơn hàng!");

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy: " + e.getMessage());
		}

		return "redirect:/vendor/orders/" + orderId;
	}

	@PostMapping("/orders/update-item-status/{orderId}/{orderItemId}")
	public String updateItemStatus(@PathVariable String orderId, @PathVariable String orderItemId,
			@RequestParam Integer newStatus, Authentication authentication, RedirectAttributes redirectAttributes) {

		try {
			User currentUser = getCurrentUser(authentication);

			orderService.updateVendorItemStatus(orderId, orderItemId, currentUser.getUserID(), newStatus);

			redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái sản phẩm thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
		}

		return "redirect:/vendor/orders/" + orderId;
	}

	@GetMapping("/revenue")
	public String viewRevenue(@RequestParam(required = false) String period,
			@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate,
			Authentication authentication, Model model) {

		User currentUser = getCurrentUser(authentication);

		// Default to last 30 days
		java.time.LocalDateTime start;
		java.time.LocalDateTime end = java.time.LocalDateTime.now();

		if (period != null) {
			switch (period) {
			case "today":
				start = java.time.LocalDateTime.now().toLocalDate().atStartOfDay();
				break;
			case "week":
				start = java.time.LocalDateTime.now().minusWeeks(1);
				break;
			case "month":
				start = java.time.LocalDateTime.now().minusMonths(1);
				break;
			case "year":
				start = java.time.LocalDateTime.now().minusYears(1);
				break;
			case "custom":
				if (startDate != null && endDate != null) {
					start = java.time.LocalDate.parse(startDate).atStartOfDay();
					end = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);
				} else {
					start = java.time.LocalDateTime.now().minusMonths(1);
				}
				break;
			default:
				start = java.time.LocalDateTime.now().minusMonths(1);
			}
		} else {
			start = java.time.LocalDateTime.now().minusMonths(1);
			period = "month";
		}

		Map<String, Object> statistics = orderService.getVendorOrderStatistics(currentUser.getUserID(), start, end);

		// Lấy dữ liệu cho biểu đồ doanh thu theo thời gian
		Map<String, Object> chartData = orderService.getRevenueChartData(currentUser.getUserID(), start, end);

		// Lấy dữ liệu cho biểu đồ phân loại sản phẩm
		Map<String, Object> categoryData = orderService.getCategoryRevenueData(currentUser.getUserID(), start, end);

		// Lấy dữ liệu top sản phẩm bán chạy
		Map<String, Object> topProductsData = orderService.getTopProductsData(currentUser.getUserID(), start, end);

		model.addAttribute("statistics", statistics);
		model.addAttribute("period", period);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);

		// Dữ liệu biểu đồ
		model.addAttribute("revenueDates", chartData.get("dates"));
		model.addAttribute("revenueValues", chartData.get("revenues"));
		model.addAttribute("categoryLabels", categoryData.get("labels"));
		model.addAttribute("categoryValues", categoryData.get("values"));
		model.addAttribute("topProductLabels", topProductsData.get("labels"));
		model.addAttribute("topProductValues", topProductsData.get("values"));
		
		// Tạo danh sách màu sắc
		String[] categoryColorsArray = {
		    "#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6",
		    "#06b6d4", "#84cc16", "#f97316", "#ec4899", "#6366f1"
		};

		List<String> categoryColors = new ArrayList<>();
		List<?> categoryLabels = (List<?>) categoryData.get("labels");
		for (int i = 0; i < categoryLabels.size(); i++) {
		    categoryColors.add(categoryColorsArray[i % categoryColorsArray.length]);
		}

		List<String> productColors = new ArrayList<>();
		List<?> topProductLabels = (List<?>) topProductsData.get("labels");
		for (int i = 0; i < topProductLabels.size(); i++) {
		    productColors.add(categoryColorsArray[i % categoryColorsArray.length]);
		}

		// Thêm vào model
		model.addAttribute("categoryColors", categoryColors);
		model.addAttribute("productColors", productColors);

		return "vendor/revenue";
	}

	// ========== API ENDPOINTS ==========
	@GetMapping("/api/subcategories/category/{categoryID}")
	@ResponseBody
	public List<Subcategory> getSubcategoriesByCategory(@PathVariable String categoryID) {
		return subcategoryService.getSubcategoriesByCategoryId(categoryID);
	}
	
	@GetMapping("/vouchers")
	public String listVouchers(
	        @RequestParam(value = "code", required = false) String code,
	        @RequestParam(value = "status", required = false) Integer status,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        Model model,
	        Principal principal) {

	    // Lấy vendor đang đăng nhập
		Optional<User> optUser = userService.findById(principal.getName());
		if (optUser.isEmpty()) {
		    throw new RuntimeException("User not found for id: " + principal.getName());
		}
		User currentUser = optUser.get();
	    String ownerId = null;

	    // Nếu là vendor thì chỉ xem voucher của chính mình
	    if (currentUser.getRole().getRolename().equals("VENDOR")) {
	        ownerId = currentUser.getUserID();
	    }

	    Page<Voucher> vouchers = voucherService.search(code, status, ownerId, PageRequest.of(page, size));

	    model.addAttribute("vouchers", vouchers.getContent());
	    model.addAttribute("page", vouchers);
	    model.addAttribute("code", code);
	    model.addAttribute("status", status);

	    return "vendor/vouchers";
	}
	
	@GetMapping("/vouchers/new")
	public String newVoucherForm(Model model) {
	    model.addAttribute("voucher", new Voucher());
	    model.addAttribute("users", userService.getAllUsers());
	    return "vendor/voucher-form";
	}

	@GetMapping("/vouchers/edit/{id}")
	public String editVoucherForm(@PathVariable String id, Model model) {
	    Voucher voucher = voucherService.findById(id)
	            .orElseThrow(() -> new RuntimeException("Voucher not found"));
	    model.addAttribute("voucher", voucher);
	    model.addAttribute("users", userService.getAllUsers());
	    return "vendor/voucher-form";
	}

	@PostMapping("/vouchers/save")
	public String saveVoucher(@ModelAttribute Voucher voucher, RedirectAttributes redirectAttributes) {
	    try {
	        if (voucher.getVoucherID() == null) {
	            voucherService.create(voucher);
	        } else {
	            voucherService.update(voucher);
	        }
	        redirectAttributes.addFlashAttribute("success", "Voucher đã được lưu thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu voucher: " + e.getMessage());
	    }
	    return "redirect:/vendor/vouchers";
	}

	@GetMapping("/vouchers/delete/{id}")
	public String deleteVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    try {
	        voucherService.delete(id);
	        redirectAttributes.addFlashAttribute("success", "Voucher đã được xóa thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa voucher: " + e.getMessage());
	    }
	    return "redirect:/vendor/vouchers";
	}

	@GetMapping("/vouchers/enable/{id}")
	public String enableVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    voucherService.enableVoucher(id);
	    redirectAttributes.addFlashAttribute("success", "Voucher đã được kích hoạt!");
	    return "redirect:/vendor/vouchers";
	}

	@GetMapping("/vouchers/disable/{id}")
	public String disableVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    voucherService.disableVoucher(id);
	    redirectAttributes.addFlashAttribute("success", "Voucher đã bị vô hiệu hóa!");
	    return "redirect:/vendor/vouchers";
	}
	// ========== POST MANAGEMENT ==========
	@GetMapping("/posts")
	public String listPosts(
	        @RequestParam(value = "title", required = false) String title,
	        @RequestParam(value = "status", required = false) Integer status,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        Authentication authentication,
	        Model model) {

	    Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());

	    User currentUser = getCurrentUser(authentication);
	    String role = currentUser.getRole().getRolename();

	    Page<Post> posts = postService.search(currentUser.getUserID(), role, title, status, pageable);

	    model.addAttribute("posts", posts.getContent());
	    model.addAttribute("page", posts);
	    model.addAttribute("title", title);
	    model.addAttribute("status", status);

	    return "vendor/posts";
	}

	@GetMapping("/posts/new")
	public String newPostForm(Model model, Principal principal) {
	    Post post = new Post();
	    post.setUser(userService.findById(principal.getName()).orElse(null)); // Gán vendor hiện tại
	    model.addAttribute("post", post);
	    return "vendor/post-form";
	}

	@GetMapping("/posts/edit/{id}")
	public String editPostForm(@PathVariable String id, Principal principal, Model model) {
	    Post post = postService.findById(id)
	            .orElseThrow(() -> new RuntimeException("Post not found"));

	    // Kiểm tra quyền: vendor chỉ được edit bài của mình
	    if (!post.getUser().getUserID().equals(principal.getName())) {
	        throw new RuntimeException("Không có quyền chỉnh sửa bài viết này");
	    }

	    model.addAttribute("post", post);
	    return "vendor/post-form";
	}

	@PostMapping("/posts/save")
	public String savePost(
	        @ModelAttribute Post post,
	        @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
	        Principal principal,
	        RedirectAttributes redirectAttributes) {
	    try {
	        // Gán vendor hiện tại
	        post.setUser(userService.findById(principal.getName()).orElse(null));

	        // Xử lý upload thumbnail
	        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
	            if (post.getPostID() != null && post.getThumbnail() != null) {
	                cloudinaryService.deleteImage(post.getThumbnail());
	            }
	            String thumbnailUrl = cloudinaryService.uploadImage(thumbnailFile);
	            post.setThumbnail(thumbnailUrl);
	        }

	        if (post.getPostID() == null) {
	            postService.create(post);
	            redirectAttributes.addFlashAttribute("success", "Bài viết đã được tạo thành công!");
	        } else {
	            // Kiểm tra quyền: chỉ update bài của mình
	            Post existingPost = postService.findById(post.getPostID())
	                    .orElseThrow(() -> new RuntimeException("Post not found"));
	            if (!existingPost.getUser().getUserID().equals(principal.getName())) {
	                throw new RuntimeException("Không có quyền cập nhật bài viết này");
	            }
	            postService.update(post);
	            redirectAttributes.addFlashAttribute("success", "Bài viết đã được cập nhật!");
	        }
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu bài viết: " + e.getMessage());
	    }
	    return "redirect:/vendor/posts";
	}

	@GetMapping("/posts/delete/{id}")
	public String deletePost(@PathVariable String id, Principal principal, RedirectAttributes redirectAttributes) {
	    try {
	        Post post = postService.findById(id)
	                .orElseThrow(() -> new RuntimeException("Post not found"));

	        // Kiểm tra quyền: chỉ xóa bài của mình
	        if (!post.getUser().getUserID().equals(principal.getName())) {
	            throw new RuntimeException("Không có quyền xóa bài viết này");
	        }

	        // Xóa thumbnail nếu có
	        if (post.getThumbnail() != null) {
	            cloudinaryService.deleteImage(post.getThumbnail());
	        }

	        postService.delete(id);
	        redirectAttributes.addFlashAttribute("success", "Bài viết đã được xóa!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa bài viết: " + e.getMessage());
	    }
	    return "redirect:/vendor/posts";
	}
}
