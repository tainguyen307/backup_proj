package com.womtech.service;

import com.womtech.entity.Product;
import com.womtech.entity.Review;
import com.womtech.entity.Subcategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductService {
	// BASIC CRUD
	List<Product> getAllProducts();

	List<Product> getActiveProducts();

	List<Product> getActiveProductsNewest();

	Optional<Product> getProductById(String id);

	Product saveProduct(Product product);

	void deleteProduct(String id);

//    // FIND BY RELATIONSHIPS
	List<Product> getProductsBySubcategory(Subcategory subcategory);

//    List<Product> getProductsBySubcategorySubcategoryID(String subcategoryID);
//    List<Product> getProductsByBrand(Brand brand);
//    List<Product> getProductsByBrandBrandID(String brandID);
//    List<Product> getProductsByCategory(Category category);
//    List<Product> getProductsByCategoryCategoryID(String categoryID);
//    List<Product> getActiveProductsByCategoryID(String categoryID);
//
//    // SEARCH & FILTER
//    List<Product> searchProducts(String keyword);
//    List<Product> searchByName(String keyword);
//    List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
//    List<Product> getProductsOnSale();
//    List<Product> getDiscountedProducts();
//
    Page<Product> searchActiveProducts(String keyword, Pageable pageable);
//    // COUNT & STATISTICS
	long getTotalCount();

//    long countByStatus(Integer status);
//    long countActiveProducts();
//    long countBySubcategory(Subcategory subcategory);
//    long countByBrand(Brand brand);
//
//    // BUSINESS LOGIC
//    //Product createProduct(String name, String description, BigDecimal price, Brand brand, Subcategory subcategory);
//    void activateProduct(String productID);
//    void deactivateProduct(String productID);
//    void setOutOfStock(String productID);
	  Page<Product> getActiveProductsByCategory(String categoryId, Pageable pageable);
	Page<Product> getAllProducts(Pageable pageable);

	Page<Product> getActiveProducts(Pageable pageable);

	Page<Product> getMyProducts(Pageable pageable);

	Optional<Product> getMyProductById(String id);

	Product createMyProduct(Product product); // tự gán owner = currentUser

	Product updateMyProduct(String productId, Product data); // chỉ cho sửa nếu là owner

	void deleteMyProduct(String productId); // chỉ cho xóa nếu là owner

	long countMyProducts();
	BigDecimal calculateTotalValueByOwnerId(String ownerUserId);

	List<Product> findByOwnerUser_UserID(String userID);
	
	Page<Review> getReviews(String productId, Pageable pageable);
}