package com.womtech.controller;

import com.womtech.entity.*;
import com.womtech.service.*;
import com.womtech.util.PasswordUtil;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private SubcategoryService subcategoryService;

	@Autowired
	private BrandService brandService;

	@Autowired
	private SpecificationService specificationService;

	@Autowired
	private InventoryService inventoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private CloudinaryService cloudinaryService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private VoucherService voucherService;
	
	@Autowired
    private PostService postService;
	
	@Autowired
	private RoleService roleService;

	// ========== DASHBOARD ==========
	@GetMapping("/dashboard")
	public String adminDashboard(Model model) {
		model.addAttribute("totalCategories", categoryService.getTotalCount());
		model.addAttribute("totalBrands", brandService.getTotalCount());
		model.addAttribute("totalSpecifications", specificationService.getTotalCount());
		model.addAttribute("totalProducts", productService.getTotalCount());
		model.addAttribute("lowStockCount", inventoryService.getLowStockCount());
		model.addAttribute("outOfStockCount", inventoryService.getOutOfStockCount());
		model.addAttribute("lowStockItems", inventoryService.getLowStockItems());
		return "admin/dashboard";
	}

	// ========== CATEGORY MANAGEMENT ==========
	@GetMapping("/categories")
	public String listCategories(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
			Model model) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		Page<Category> categoryPage = categoryService.getAllCategories(pageable);

		model.addAttribute("categories", categoryPage.getContent());
		model.addAttribute("page", categoryPage);
		return "admin/categories";
	}

	@GetMapping("/categories/new")
	public String newCategoryForm(Model model) {
		model.addAttribute("category", new Category());
		return "admin/category-form";
	}

	@GetMapping("/categories/edit/{id}")
	public String editCategoryForm(@PathVariable String id, Model model) {
		Category category = categoryService.getCategoryById(id)
				.orElseThrow(() -> new RuntimeException("Category not found"));
		model.addAttribute("category", category);
		return "admin/category-form";
	}

	@PostMapping("/categories/save")
	public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
		try {
			System.out.println(">>> [DEBUG] Gọi hàm saveCategory()");
			System.out.println(">>> [DEBUG] Dữ liệu nhận được từ form: ID: " + category.getCategoryID() + ", Name: "
					+ category.getName() + ", Status: " + category.getStatus());
			categoryService.saveCategory(category);
			redirectAttributes.addFlashAttribute("success", "Danh mục đã được lưu thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu danh mục: " + e.getMessage());
		}
		return "redirect:/admin/categories";
	}

	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable String id, RedirectAttributes redirectAttributes) {
		try {
			categoryService.deleteCategory(id);
			redirectAttributes.addFlashAttribute("success", "Danh mục đã được xóa thành công!");
		} catch (RuntimeException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi xóa danh mục: " + e.getMessage());
		}
		return "redirect:/admin/categories";
	}

	// ========== SUBCATEGORY MANAGEMENT ==========
	@GetMapping("/subcategories/category/{categoryID}")
	public String listSubcategories(@PathVariable String categoryID, Model model) {
		Category category = categoryService.getCategoryById(categoryID)
				.orElseThrow(() -> new RuntimeException("Category not found"));
		model.addAttribute("category", category);
		model.addAttribute("subcategories", subcategoryService.getSubcategoriesByCategoryId(categoryID));
		return "admin/subcategories";
	}

	@GetMapping("/subcategories/{subcategoryId}/products")
	public String listProductsBySubcategory(@PathVariable String subcategoryId, Model model) {
		Subcategory subcategory = subcategoryService.getSubcategoryById(subcategoryId)
				.orElseThrow(() -> new RuntimeException("Subcategory not found"));
		List<Product> products = productService.getProductsBySubcategory(subcategory);

		model.addAttribute("subcategory", subcategory);
		model.addAttribute("category", subcategory.getCategory());
		model.addAttribute("products", products);
		model.addAttribute("productCount", products.size());

		return "admin/product-by-subcategory";
	}

	@GetMapping("/subcategories/new/{categoryID}")
	public String newSubcategoryForm(@PathVariable String categoryID, Model model) {
		Category category = categoryService.getCategoryById(categoryID)
				.orElseThrow(() -> new RuntimeException("Category not found"));
		Subcategory subcategory = new Subcategory();
		subcategory.setCategory(category);
		model.addAttribute("subcategory", subcategory);
		model.addAttribute("category", category);
		return "admin/subcategory-form";
	}

	@GetMapping("/subcategories/edit/{id}")
	public String editSubcategoryForm(@PathVariable String id, Model model) {
		Subcategory subcategory = subcategoryService.getSubcategoryById(id)
				.orElseThrow(() -> new RuntimeException("Subcategory not found"));
		model.addAttribute("subcategory", subcategory);
		model.addAttribute("category", subcategory.getCategory());
		return "admin/subcategory-form";
	}

	@PostMapping("/subcategories/save")
	public String saveSubcategory(@ModelAttribute Subcategory subcategory, RedirectAttributes redirectAttributes) {
		try {
			subcategoryService.saveSubcategory(subcategory);
			redirectAttributes.addFlashAttribute("success", "Danh mục con đã được lưu thành công!");
			return "redirect:/admin/subcategories/category/" + subcategory.getCategory().getCategoryID();
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu danh mục con: " + e.getMessage());
			return "redirect:/admin/subcategories";
		}
	}

	@GetMapping("/subcategories/delete/{id}")
	public String deleteSubcategory(@PathVariable String id, RedirectAttributes redirectAttributes) {
		try {
			Subcategory subcategory = subcategoryService.getSubcategoryById(id)
					.orElseThrow(() -> new RuntimeException("Subcategory not found"));
			String categoryID = subcategory.getCategory().getCategoryID();
			subcategoryService.deleteSubcategory(id);
			redirectAttributes.addFlashAttribute("success", "Danh mục con đã được xóa thành công!");
			return "redirect:/admin/subcategories/category/" + categoryID;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa danh mục con: " + e.getMessage());
			return "redirect:/admin/subcategories";
		}
	}

	// ========== BRAND MANAGEMENT ==========
	@GetMapping("/brands")
	public String listBrands(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
			Model model) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		Page<Brand> brandPage = brandService.getAllBrands(pageable);

		model.addAttribute("brands", brandPage.getContent());
		model.addAttribute("page", brandPage);
		return "admin/brands";
	}

	@GetMapping("/brands/new")
	public String newBrandForm(Model model) {
		model.addAttribute("brand", new Brand());
		return "admin/brand-form";
	}

	@GetMapping("/brands/edit/{id}")
	public String editBrandForm(@PathVariable String id, Model model) {
		Brand brand = brandService.getBrandById(id).orElseThrow(() -> new RuntimeException("Brand not found"));
		model.addAttribute("brand", brand);
		return "admin/brand-form";
	}

	@PostMapping("/brands/save")
	public String saveBrand(@ModelAttribute Brand brand, RedirectAttributes redirectAttributes) {
		try {
			brandService.saveBrand(brand);
			redirectAttributes.addFlashAttribute("success", "Thương hiệu đã được lưu thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu thương hiệu: " + e.getMessage());
		}
		return "redirect:/admin/brands";
	}

	@GetMapping("/brands/delete/{id}")
	public String deleteBrand(@PathVariable String id, RedirectAttributes redirectAttributes) {
		System.out.println("Gọi API xóa thương hiệu với ID: " + id);
		try {
			brandService.deleteBrand(id);
			System.out.println("Xóa thành công, thêm thông báo success");
			redirectAttributes.addFlashAttribute("success", "Thương hiệu đã được xóa thành công!");
		} catch (RuntimeException e) {
			System.out.println("Lỗi RuntimeException: " + e.getMessage());
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		} catch (Exception e) {
			System.out.println("Lỗi Exception: " + e.getMessage());
			redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa thương hiệu: " + e.getMessage());
		}
		System.out.println("Chuyển hướng đến /admin/brands");
		return "redirect:/admin/brands";
	}

	// ========== PRODUCT MANAGEMENT ==========
	@GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String subcategoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        // Get all products first, then filter
        List<Product> allProducts = productService.getAllProducts();
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            allProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower) ||
                                p.getProductID().toLowerCase().contains(searchLower))
                    .toList();
        }
        
        // Apply category filter
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            allProducts = allProducts.stream()
                    .filter(p -> p.getSubcategory().getCategory().getCategoryID().equals(categoryId))
                    .toList();
        }
        
        // Apply subcategory filter
        if (subcategoryId != null && !subcategoryId.trim().isEmpty()) {
            allProducts = allProducts.stream()
                    .filter(p -> p.getSubcategory().getSubcategoryID().equals(subcategoryId))
                    .toList();
        }
        
        // Apply brand filter
        if (brandId != null && !brandId.trim().isEmpty()) {
            allProducts = allProducts.stream()
                    .filter(p -> p.getBrand().getBrandID().equals(brandId))
                    .toList();
        }
        
        // Apply status filter
        if (status != null) {
            allProducts = allProducts.stream()
                    .filter(p -> p.getStatus().equals(status))
                    .toList();
        }
        
        // Sort by createAt descending
        allProducts = allProducts.stream()
                .sorted((p1, p2) -> p2.getCreateAt().compareTo(p1.getCreateAt()))
                .toList();
        
        // Manual pagination
        int start = Math.min(page * size, allProducts.size());
        int end = Math.min(start + size, allProducts.size());
        List<Product> pagedProducts = allProducts.subList(start, end);
        
        // Create Page object for pagination fragment
        Pageable pageable = PageRequest.of(page, size);
        int totalPages = (int) Math.ceil((double) allProducts.size() / size);
        
        // Add attributes to model
        model.addAttribute("products", pagedProducts);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        
        // If category is selected, load its subcategories
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            model.addAttribute("subcategories", subcategoryService.getSubcategoriesByCategoryId(categoryId));
        }
        
        // Create custom page object
        org.springframework.data.domain.PageImpl<Product> pageImpl = 
            new org.springframework.data.domain.PageImpl<>(pagedProducts, pageable, allProducts.size());
        model.addAttribute("page", pageImpl);
        
        return "admin/products";
    }
	@GetMapping("/products/new")
	public String newProductForm(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("users", userService.getAllUsers());

		return "admin/product-form";
	}

	@GetMapping("/products/edit/{id}")
	public String editProductForm(@PathVariable String id, Model model) {
		Product product = productService.getProductById(id)
				.orElseThrow(() -> new RuntimeException("Product not found"));
		model.addAttribute("product", product);
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("users", userService.getAllUsers());
		return "admin/product-form";
	}

	@PostMapping("/products/save")
	public String saveProduct(@ModelAttribute Product product,
			@RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
			RedirectAttributes redirectAttributes) {
		try {
			// Handle thumbnail upload
			if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
				// Delete old thumbnail if exists
				if (product.getProductID() != null && product.getThumbnail() != null) {
					cloudinaryService.deleteImage(product.getThumbnail());
				}

				// Upload new thumbnail
				String thumbnailUrl = cloudinaryService.uploadImage(thumbnailFile);
				product.setThumbnail(thumbnailUrl);
			}

			productService.saveProduct(product);
			redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được lưu thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu sản phẩm: " + e.getMessage());
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable String id, RedirectAttributes redirectAttributes) {
		try {
			// Get product to delete thumbnail
			Product product = productService.getProductById(id).orElse(null);
			if (product != null && product.getThumbnail() != null) {
				cloudinaryService.deleteImage(product.getThumbnail());
			}

			productService.deleteProduct(id);
			redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được xóa thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa sản phẩm: " + e.getMessage());
		}
		return "redirect:/admin/products";
	}

	// ========== SPECIFICATION MANAGEMENT (UPDATED) ==========

	@GetMapping("/specifications")
	public String listSpecifications(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, Model model) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("product.productID").ascending());
		Page<Specification> specificationPage = specificationService.getAllSpecifications(pageable);
		List<Product> allProducts = productService.getAllProducts(); // Cần có service method này
		
		model.addAttribute("products", allProducts); // THÊM DÒNG NÀY
		model.addAttribute("specifications", specificationPage.getContent());
		model.addAttribute("page", specificationPage);
		return "admin/specifications";
	}

	@GetMapping("/specifications/product/{productID}")
	public String listSpecificationsByProduct(@PathVariable String productID, Model model) {
		Product product = productService.getProductById(productID)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		model.addAttribute("product", product);
		model.addAttribute("specifications", product.getSpecifications());

		return "admin/specifications-by-product";
	}

	@GetMapping("/specifications/new/{productID}")
	public String newSpecificationFormForProduct(@PathVariable String productID, Model model) {
		Product product = productService.getProductById(productID)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

		Specification specification = new Specification();
		specification.setProduct(product);

		model.addAttribute("specification", specification);
		model.addAttribute("product", product);

		return "admin/specification-form";
	}

	@GetMapping("/specifications/new")
	public String newSpecificationForm(Model model) {
		model.addAttribute("specification", new Specification());
		model.addAttribute("products", productService.getAllProducts());
		return "admin/specification-form";
	}

	@GetMapping("/specifications/edit/{id}")
	public String editSpecificationForm(@PathVariable String id, Model model) {
		Specification specification = specificationService.getSpecificationByID(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy thông số kỹ thuật"));

		model.addAttribute("specification", specification);
		model.addAttribute("product", specification.getProduct());
		model.addAttribute("products", productService.getAllProducts());

		return "admin/specification-form";
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
				return "redirect:/admin/specifications/product/" + specification.getProduct().getProductID();
			}

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu thông số: " + e.getMessage());
		}

		return "redirect:/admin/specifications";
	}

	/**
	 * Xóa thông số kỹ thuật
	 */
	@GetMapping("/specifications/delete/{id}")
	public String deleteSpecification(@PathVariable String id, RedirectAttributes redirectAttributes) {
		try {
			Specification specification = specificationService.getSpecificationByID(id)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy thông số kỹ thuật"));

			String productID = specification.getProduct().getProductID();
			specificationService.deleteSpecification(id);

			redirectAttributes.addFlashAttribute("success", "Thông số kỹ thuật đã được xóa thành công!");

			// Redirect về trang specifications của product
			return "redirect:/admin/specifications/product/" + productID;

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa thông số: " + e.getMessage());
			return "redirect:/admin/specifications";
		}
	}

	// ========== INVENTORY MANAGEMENT ==========

	@GetMapping("/inventory")
	public String listInventory(@RequestParam(value = "filter", defaultValue = "all") String filter,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("updateAt").descending());
		Page<Inventory> inventoryPage;

		switch (filter.toLowerCase()) {
		case "low-stock":
			inventoryPage = inventoryService.getLowStockItems(pageable);
			model.addAttribute("title", "Sản phẩm sắp hết hàng");
			break;
		case "out-of-stock":
			inventoryPage = inventoryService.getOutOfStockItems(pageable);
			model.addAttribute("title", "Sản phẩm đã hết hàng");
			break;
		default:
			inventoryPage = inventoryService.getAllInventory(pageable);
			model.addAttribute("title", "Tất cả sản phẩm tồn kho");
			break;
		}

		model.addAttribute("inventoryItems", inventoryPage.getContent());
		model.addAttribute("page", inventoryPage);
		model.addAttribute("filter", filter);

		// Dữ liệu tổng quan
		model.addAttribute("totalCount", inventoryService.getTotalCount());
		model.addAttribute("lowStockCount", inventoryService.getLowStockCount());
		model.addAttribute("outOfStockCount", inventoryService.getOutOfStockCount());

		return "admin/inventory";

	}

	@GetMapping("/inventory/new")
	public String newInventoryForm(Model model) {
		model.addAttribute("inventory", new Inventory());
		model.addAttribute("products", productService.getAllProducts());
		model.addAttribute("locations", inventoryService.getAllLocations());
		return "admin/inventory-form";
	}

	@GetMapping("/inventory/edit/{id}")
	public String editInventoryForm(@PathVariable String id, Model model) {
		Inventory inventory = inventoryService.getInventoryByID(id)
				.orElseThrow(() -> new RuntimeException("Inventory not found"));
		model.addAttribute("inventory", inventory);
		model.addAttribute("products", productService.getAllProducts());
		model.addAttribute("locations", inventoryService.getAllLocations());
		return "admin/inventory-form";
	}

	@PostMapping("/inventory/save")
	public String saveInventory(@ModelAttribute Inventory inventory, RedirectAttributes redirectAttributes) {
		try {
			if (inventory.getInventoryID() != null && inventory.getInventoryID().isEmpty()) {
	            inventory.setInventoryID(null);
	        }

	        inventoryService.saveInventory(inventory);
	        redirectAttributes.addFlashAttribute("success", "Tồn kho đã được cập nhật thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật tồn kho: " + e.getMessage());
		}
		return "redirect:/admin/inventory";
	}

	@PostMapping("/inventory/restock/{id}")
	public String restockInventory(@PathVariable String id, @RequestParam Integer quantity,
			RedirectAttributes redirectAttributes) {
		try {
			inventoryService.restockInventory(id, quantity);
			redirectAttributes.addFlashAttribute("success", "Nhập kho thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi nhập kho: " + e.getMessage());
		}
		return "redirect:/admin/inventory";
	}
	@GetMapping("/users")
	public String listUsers(
	        @RequestParam(value = "search", required = false) String search,
	        @RequestParam(value = "role", required = false) String role,
	        @RequestParam(value = "status", required = false) String statusStr,
	        Pageable pageable,
	        Model model) {

	    Integer status = null;
	    if (statusStr != null && !statusStr.isBlank()) {
	        try {
	            status = Integer.parseInt(statusStr);
	        } catch (NumberFormatException e) {
	            status = null;
	        }
	    }

	    Page<User> page = userService.searchUsers(
	            (search != null && !search.isBlank()) ? search : null,
	            (role != null && !role.isBlank()) ? role : null,
	            status,
	            pageable
	    );

	    model.addAttribute("users", page.getContent());
	    model.addAttribute("page", page);
	    return "admin/users";
	}
	@GetMapping("/users/new")
	public String newUserForm(Model model) {
	    model.addAttribute("user", new User());
	    model.addAttribute("roles", roleService.findAll());
	    return "admin/user-form";
	}

	@PostMapping("/users/save")
    public String saveUser(
            @ModelAttribute("user") User user,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @RequestParam(value = "passwordRaw", required = false) String pwRaw
    ) {

        // --- ROLE ---
        if (user.getRole() != null && user.getRole().getRoleID() != null) {
            Role r = roleService.findById(user.getRole().getRoleID())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(r);
        }

        // --- NEW USER ---
        if (user.getUserID() == null || user.getUserID().isEmpty()) {

            // Password bắt buộc
            if (pwRaw == null || pwRaw.isEmpty()) {
                throw new RuntimeException("Password không được để trống khi tạo mới user");
            }
            user.setPassword(PasswordUtil.encode(pwRaw));

            // Avatar
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String uploadUrl = cloudinaryService.uploadImage(avatarFile);
                user.setAvatar(uploadUrl);
            }

            // Save new user
            userService.save(user);
            return "redirect:/admin/users";
        }

        // --- UPDATE USER ---
        User existing = userService.findById(user.getUserID())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Cập nhật từng field từ form
        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        existing.setStatus(user.getStatus());

        // Password: nếu có pwRaw mới thì encode, không thì giữ nguyên
        if (pwRaw != null && !pwRaw.isEmpty()) {
            existing.setPassword(PasswordUtil.encode(pwRaw));
        }

        // Avatar
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Xóa avatar cũ
            if (existing.getAvatar() != null) {
                cloudinaryService.deleteImage(existing.getAvatar());
            }
            String uploadUrl = cloudinaryService.uploadImage(avatarFile);
            existing.setAvatar(uploadUrl);
        }

        // Save update
        userService.save(existing);

        return "redirect:/admin/users";
    }
	@GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable String id, Model model, HttpSession session) {

        String currentUserId = (String) session.getAttribute("userID");

        if (id.equals(currentUserId)) {
            throw new RuntimeException("Admin cannot edit your own account.");
        }

        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.findAll());

        return "admin/user-form";
    }
	@GetMapping("/users/lock/{id}")
	public String lockUser(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
	    Optional<User> currentUserOpt = userService.findById(currentUserId);
	    
	    if (currentUserOpt.isPresent() && currentUserOpt.get().getUserID().equals(id)) {
	        redirectAttributes.addFlashAttribute("error", "Không thể tự khóa chính mình!");
	        return "redirect:/admin/users";
	    }

	    try {
	        userService.lockUser(id);
	        redirectAttributes.addFlashAttribute("success", "User đã bị khóa thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi khóa user: " + e.getMessage());
	    }

	    return "redirect:/admin/users";
	}

	@GetMapping("/users/unlock/{id}")
	public String unlockUser(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    try {
	        userService.unlockUser(id);
	        redirectAttributes.addFlashAttribute("success", "User đã được mở khóa thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi mở khóa user: " + e.getMessage());
	    }
	    return "redirect:/admin/users";
	}
	
	@GetMapping("/users/delete/{id}")
	public String deleteUser(
	        @PathVariable("id") String userId,
	        Principal principal,
	        RedirectAttributes redirectAttributes
	) {
	    try {
	        // Không cho tự xoá chính mình
	        if (principal != null && userId.equals(principal.getName())) {
	            redirectAttributes.addFlashAttribute("error", "Bạn không thể tự xoá chính tài khoản của mình.");
	            return "redirect:/admin/users";
	        }

	        userService.deleteUserById(userId);
	        redirectAttributes.addFlashAttribute("success", "Xoá tài khoản thành công!");

	    } catch (EmptyResultDataAccessException e) {
	        // deleteById() ném ra nếu không tồn tại
	        redirectAttributes.addFlashAttribute("error", "Không tìm thấy user với ID: " + userId);
	    } catch (DataIntegrityViolationException e) {
	        // Lỗi ràng buộc khoá ngoại: user còn dữ liệu liên quan
	        redirectAttributes.addFlashAttribute("error",
	                "Không thể xoá do tài khoản còn dữ liệu liên quan (đơn hàng/sản phẩm/bài viết...). " +
	                "Hãy xoá/đổi chủ sở hữu dữ liệu liên quan trước.");
	    } catch (RuntimeException e) {
	        // Nếu bạn ném RuntimeException("User not found ...") trong service
	        redirectAttributes.addFlashAttribute("error", e.getMessage());
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi xoá user: " + e.getMessage());
	    }
	    return "redirect:/admin/users";
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

	    return "admin/vouchers";
	}

	@GetMapping("/vouchers/new")
	public String newVoucherForm(Model model) {
	    model.addAttribute("voucher", new Voucher());
	    model.addAttribute("users", userService.getAllUsers());
	    return "admin/voucher-form";
	}

	@GetMapping("/vouchers/edit/{id}")
	public String editVoucherForm(@PathVariable String id, Model model) {
	    Voucher voucher = voucherService.findById(id)
	            .orElseThrow(() -> new RuntimeException("Voucher not found"));
	    model.addAttribute("voucher", voucher);
	    model.addAttribute("users", userService.getAllUsers());
	    return "admin/voucher-form";
	}

	@PostMapping("/vouchers/save")
	public String saveVoucher(@ModelAttribute Voucher voucher, RedirectAttributes redirectAttributes) {
	    try {
	        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
	        User owner = userService.findById(userId)
	                .orElseThrow(() -> new IllegalStateException("Không tìm thấy vendor đăng nhập"));
	        voucher.setOwner(owner);

	        if (voucher.getExpire_date() != null && voucher.getExpire_date().isBefore(LocalDateTime.now())) {
	            redirectAttributes.addFlashAttribute("error", "Ngày hết hạn phải sau ngày hiện tại!");
	            return "redirect:/admin/vouchers";
	        }

	        if (voucher.getVoucherID() == null || voucher.getVoucherID().isBlank()) {
	            // Thêm mới
	            voucherService.create(voucher);
	        } else {
	            // Cập nhật
	            voucherService.update(voucher);
	        }

	        redirectAttributes.addFlashAttribute("success", "Voucher đã được lưu thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu voucher: " + e.getMessage());
	    }
	    return "redirect:/admin/vouchers";
	}

	@GetMapping("/vouchers/delete/{id}")
	public String deleteVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    try {
	        voucherService.delete(id);
	        redirectAttributes.addFlashAttribute("success", "Voucher đã được xóa thành công!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa voucher: " + e.getMessage());
	    }
	    return "redirect:/admin/vouchers";
	}

	@GetMapping("/vouchers/enable/{id}")
	public String enableVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    voucherService.enableVoucher(id);
	    redirectAttributes.addFlashAttribute("success", "Voucher đã được kích hoạt!");
	    return "redirect:/admin/vouchers";
	}

	@GetMapping("/vouchers/disable/{id}")
	public String disableVoucher(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    voucherService.disableVoucher(id);
	    redirectAttributes.addFlashAttribute("success", "Voucher đã bị vô hiệu hóa!");
	    return "redirect:/admin/vouchers";
	}
	
	// ========== POST MANAGEMENT ==========
	@GetMapping("/posts")
	public String listPosts(
	        @RequestParam(value = "title", required = false) String title,
	        @RequestParam(value = "status", required = false) Integer status,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        Principal principal,   // <-- thay Authentication bằng Principal
	        Model model) {

	    Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());

	    User currentUser = userService.findById(principal.getName())
	            .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));

	    String role = currentUser.getRole().getRolename();

	    Page<Post> posts = postService.search(currentUser.getUserID(), role, title, status, pageable);

	    model.addAttribute("posts", posts.getContent());
	    model.addAttribute("page", posts);
	    model.addAttribute("title", title);
	    model.addAttribute("status", status);

	    return role.equalsIgnoreCase("ADMIN") ? "admin/posts" : "vendor/posts";
	}

	@GetMapping("/posts/new")
	public String newPostForm(Model model) {
	    model.addAttribute("post", new Post());
	    model.addAttribute("users", userService.getAllUsers());
	    return "admin/post-form";
	}

	@GetMapping("/posts/edit/{id}")
	public String editPostForm(@PathVariable String id, Model model) {
	    Post post = postService.findById(id)
	            .orElseThrow(() -> new RuntimeException("Post not found"));
	    model.addAttribute("post", post);
	    model.addAttribute("users", userService.getAllUsers());
	    return "admin/post-form";
	}

	@PostMapping("/posts/save")
	public String savePost(
	        @ModelAttribute Post post,
	        @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile, 
	        Principal principal,
	        RedirectAttributes redirectAttributes) {

	    try {
	        // --- Set user chắc chắn tồn tại ---
	        User currentUser = userService.findById(principal.getName())
	                .orElseThrow(() -> new RuntimeException("Người dùng đăng nhập không tồn tại"));
	        post.setUser(currentUser);

	        // --- Upload thumbnail nếu có ---
	        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
	            if (post.getPostID() != null && post.getThumbnail() != null) {
	                cloudinaryService.deleteImage(post.getThumbnail());
	            }
	            String thumbnailUrl = cloudinaryService.uploadImage(thumbnailFile);
	            post.setThumbnail(thumbnailUrl);
	        }

	        if (post.getPostID() == null || post.getPostID().isEmpty()) {
	            // --- Tạo mới bài viết ---
	        	post.setPostID(null);
	            post.setCreateAt(LocalDateTime.now());
	            post.setUpdateAt(LocalDateTime.now());
	            postService.create(post);
	            redirectAttributes.addFlashAttribute("success", "Bài viết đã được tạo thành công!");
	        } else {
	            // --- Cập nhật bài viết ---
	            Post existing = postService.findById(post.getPostID())
	                    .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

	            existing.setTitle(post.getTitle());
	            existing.setType(post.getType());
	            existing.setContent(post.getContent());
	            existing.setThumbnail(post.getThumbnail());
	            existing.setStatus(post.getStatus());
	            existing.setUpdateAt(LocalDateTime.now());

	            postService.update(existing);
	            redirectAttributes.addFlashAttribute("success", "Bài viết đã được cập nhật!");
	        }

	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu bài viết: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return "redirect:/admin/posts";
	}

	@GetMapping("/posts/delete/{id}")
	public String deletePost(@PathVariable String id, RedirectAttributes redirectAttributes) {
	    try {
	        postService.delete(id);
	        redirectAttributes.addFlashAttribute("success", "Bài viết đã được xóa!");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa bài viết: " + e.getMessage());
	    }
	    return "redirect:/admin/posts";
	}
}