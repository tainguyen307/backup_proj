package com.womtech.service.impl;

import com.womtech.entity.Brand;
import com.womtech.entity.Category;
import com.womtech.entity.Product;
import com.womtech.entity.Review;
import com.womtech.entity.Subcategory;
import com.womtech.repository.ProductRepository;
import com.womtech.repository.ReviewRepository;
import com.womtech.repository.UserRepository;
import com.womtech.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;
	private final UserRepository userRepository = null;
	@Autowired
    private ReviewRepository reviewRepository;

	private String currentUserId() {
		var ctx = org.springframework.security.core.context.SecurityContextHolder.getContext();
		var auth = ctx != null ? ctx.getAuthentication() : null;
		return (auth != null && auth.getName() != null) ? auth.getName() : null; // getName() = userId
	}

	private boolean hasAdminRole() {
		var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			return false;
		return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
	
	public BigDecimal calculateTotalValueByOwnerId(String ownerUserId) {
        List<Product> myProducts = productRepository.findByOwnerUser_UserID(ownerUserId);

        BigDecimal totalValue = BigDecimal.ZERO;

        for (Product p : myProducts) {
            BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            BigDecimal discountPrice = p.getDiscount_price();

            // Nếu có giá giảm hợp lệ thì dùng discount_price, ngược lại dùng price
            if (discountPrice != null
                    && discountPrice.compareTo(BigDecimal.ZERO) > 0
                    && discountPrice.compareTo(price) < 0) {
                totalValue = totalValue.add(discountPrice);
            } else {
                totalValue = totalValue.add(price);
            }
        }

        return totalValue;
    }
	
	public List<Product> findByOwnerUser_UserID(String userID){
		return productRepository.findByOwnerUser_UserID(userID);
	}



	// BASIC CRUD

	@Override
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Override
	public Page<Product> getAllProducts(Pageable pageable) {
		return productRepository.findAll(pageable);
	}

	@Override
	public List<Product> getActiveProducts() {
		return productRepository.findByStatus(1);
	}

	@Override
	public Page<Product> getActiveProducts(Pageable pageable) {
		return productRepository.findByStatus(1, pageable);
	}

	@Override
	public List<Product> getActiveProductsNewest() {
		return productRepository.findActiveProductsOrderByNewest();
	}

	@Override
	public Optional<Product> getProductById(String id) {
		return productRepository.findById(id);
	}

	@Override
	public Product saveProduct(Product product) {
		if (product.getProductID() != null && product.getProductID().trim().isEmpty()) {
			product.setProductID(null); // <- ép về null để Hibernate tự sinh UUID
		}
		return productRepository.save(product);
	}

	@Override
	public void deleteProduct(String id) {
		productRepository.deleteById(id);
	}

//    // FIND BY RELATIONSHIPS
//
    @Override
    public List<Product> getProductsBySubcategory(Subcategory subcategory) {
        return productRepository.findBySubcategory(subcategory);
    }
    
    @Override
    public Page<Product> getActiveProductsByCategory(String categoryId, Pageable pageable) {
        return productRepository.findBySubcategory_Category_CategoryIDAndStatusTrue(categoryId, pageable);
    }
    
    @Override
    public Page<Product> searchActiveProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findByStatus(1, pageable); // nếu ko có keyword thì trả sp active
        }
        return productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, 1, pageable);
    }
//
//    @Override
//    public List<Product> getProductsBySubcategorySubcategoryID(String subcategoryID) {
//        return productRepository.findBySubcategorySubcategoryID(subcategoryID);
//    }
//
//    @Override
//    public List<Product> getProductsByBrand(Brand brand) {
//        return productRepository.findByBrand(brand);
//    }
//
//    @Override
//    public List<Product> getProductsByBrandBrandID(String brandID) {
//        return productRepository.findByBrandBrandID(brandID);
//    }
//
//    @Override
//    public List<Product> getProductsByCategory(Category category) {
//        return productRepository.findBySubcategoryCategory(category);
//    }
//
//    @Override
//    public List<Product> getProductsByCategoryCategoryID(String categoryID) {
//        return productRepository.findBySubcategoryCategoryCategoryID(categoryID);
//    }
//
//    @Override
//    public List<Product> getActiveProductsByCategoryID(String categoryId) {
//        return productRepository.findActiveByCategoryID(categoryId);
//    }
//
//    // SEARCH & FILTER
//
//    @Override
//    public List<Product> searchProducts(String keyword) {
//        if (keyword == null || keyword.trim().isEmpty()) {
//            return getActiveProducts();
//        }
//        return productRepository.searchProducts(keyword.trim());
//    }
//
//    @Override
//    public List<Product> searchByName(String keyword) {
//        return productRepository.findByNameContainingIgnoreCase(keyword);
//    }
//
//    @Override
//    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
//        return productRepository.findByPriceBetween(minPrice, maxPrice);
//    }
//
//    @Override
//    public List<Product> getProductsOnSale() {
//        return productRepository.findProductsOnSale();
//    }
//
//    @Override
//    public List<Product> getDiscountedProducts() {
//        return productRepository.findByDiscount_priceIsNotNull();
//    }
//
//    // COUNT & STATISTICS
//
	@Override
	public long getTotalCount() {
		return productRepository.count();
	}

	@Override
	@PreAuthorize("hasRole('VENDOR')")
	public Page<Product> getMyProducts(Pageable pageable) {
		return productRepository.findByOwnerUser_UserID(currentUserId(), pageable);
	}

	@Override
	@PreAuthorize("hasRole('VENDOR')")
	public Optional<Product> getMyProductById(String id) {
		return productRepository.findByProductIDAndOwnerUser_UserID(id, currentUserId());
	}

	@Override
	@PreAuthorize("hasRole('VENDOR')")
	public Product createMyProduct(Product product) {
		// Gán owner = current user
		var uid = currentUserId();
		var me = userRepository.getReferenceById(uid);

		// đảm bảo để Hibernate tự sinh id khi cần
		if (product.getProductID() != null && product.getProductID().trim().isEmpty()) {
			product.setProductID(null);
		}

		product.setOwnerUser(me);
		return productRepository.save(product);
	}

	@Override
	@PreAuthorize("hasRole('VENDOR')")
	public Product updateMyProduct(String productId, Product data) {
		var uid = currentUserId();

		var p = productRepository.findByProductIDAndOwnerUser_UserID(productId, uid)
				.orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
						"Bạn không sở hữu sản phẩm này hoặc sản phẩm không tồn tại"));

		// Cập nhật các field cho phép
		p.setName(data.getName());
		p.setPrice(data.getPrice());
		p.setDiscount_price(data.getDiscount_price());
		p.setThumbnail(data.getThumbnail());
		p.setDescription(data.getDescription());
		p.setBrand(data.getBrand());
		p.setSubcategory(data.getSubcategory());
		p.setStatus(data.getStatus());

		return productRepository.save(p);
	}

	@Override
	@PreAuthorize("hasRole('VENDOR')")
	public void deleteMyProduct(String productId) {
		var uid = currentUserId();
		var p = productRepository.findByProductIDAndOwnerUser_UserID(productId, uid)
				.orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
						"Bạn không sở hữu sản phẩm này hoặc sản phẩm không tồn tại"));
		productRepository.delete(p);
	}

	@Override
	@PreAuthorize("hasRole('VENDOR')")
	public long countMyProducts() {
		return productRepository.countByOwnerUser_UserID(currentUserId());
	}

//    @Override
//    public long countByStatus(Integer status) {
//        return productRepository.countByStatus(status);
//    }
//
//    @Override
//    public long countActiveProducts() {
//        return productRepository.countByStatus(1);
//    }
//
//    @Override
//    public long countBySubcategory(Subcategory subcategory) {
//        return productRepository.countBySubcategory(subcategory);
//    }
//
//    @Override
//    public long countByBrand(Brand brand) {
//        return productRepository.countByBrand(brand);
//    }
//
//    // BUSINESS LOGIC
//
//    //@Override
	////    public Product createProduct(String name, String description, BigDecimal price, 
////                                Brand brand, Subcategory subcategory) {
////        Product product = new Product(name, description, price, brand, subcategory);
////        return saveProduct(product);
////    }
//
//    @Override
//    public void activateProduct(String productId) {
//        Optional<Product> productOpt = getProductById(productId);
//        if (productOpt.isPresent()) {
//            Product product = productOpt.get();
//            product.setStatus(1);
//            saveProduct(product);
//        }
//    }
//
//    @Override
//    public void deactivateProduct(String productId) {
//        Optional<Product> productOpt = getProductById(productId);
//        if (productOpt.isPresent()) {
//            Product product = productOpt.get();
//            product.setStatus(0);
//            saveProduct(product);
//        }
//    }
//
//    @Override
//    public void setOutOfStock(String productId) {
//        Optional<Product> productOpt = getProductById(productId);
//        if (productOpt.isPresent()) {
//            Product product = productOpt.get();
//            product.setStatus(2);
//            saveProduct(product);
//        }
//    }
	
// Review
	@Override
	public Page<Review> getReviews(String productId, Pageable pageable) {
	    return reviewRepository.findByProduct_ProductIDAndStatus(productId, 1, pageable);
	}
}