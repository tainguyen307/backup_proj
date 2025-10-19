package com.womtech.repository;

import com.womtech.entity.Product;
import com.womtech.entity.Subcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
//    // Find by relationships
//    
//    List<Product> findBySubcategorySubcategoryID(String subcategoryID);
//    
//    List<Product> findByBrand(Brand brand);
//    
//    List<Product> findByBrandBrandID(String brandID);
//    
//    List<Product> findBySubcategoryCategory(Category category);
//    
//    List<Product> findBySubcategoryCategoryCategoryID(String categoryID);
//    
//    // Find by status

		Page<Product> findBySubcategory_Category_CategoryIDAndStatusTrue(String categoryId, Pageable pageable);
//    
//    List<Product> findByStatusOrderByCreateAtDesc(Integer status);
//    
//    // Search
//    @Query("SELECT p FROM Product p WHERE " +
//           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//           "LOWER(p.brand.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//           "LOWER(p.subcategory.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//           "LOWER(p.subcategory.category.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
//           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
//    List<Product> searchProducts(@Param("keyword") String keyword);
//    
//    List<Product> findByNameContainingIgnoreCase(String keyword);
	Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
	Page<Product> findByNameContainingIgnoreCaseAndStatus(String keyword, Integer status, Pageable pageable);
//    
//    // Price queries
//    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
//    
//    List<Product> findByDiscount_priceIsNotNull();
//    
//    // Count queries
//    long countBySubcategory(Subcategory subcategory);
//    
//    long countByBrand(Brand brand);
//    
//    long countByStatus(Integer status);
//    
    // Advanced queries

//    
//    @Query("SELECT p FROM Product p WHERE p.subcategory.category.categoryID = :categoryID AND p.status = 1")
//    List<Product> findActiveByCategoryID(@Param("categoryID") String categoryID);
//    
//    @Query("SELECT p FROM Product p WHERE p.discount_price IS NOT NULL AND p.status = 1 ORDER BY p.discount_price ASC")
//    List<Product> findProductsOnSale();


	// ====== Giữ nguyên các method sẵn có ======
	List<Product> findBySubcategory(Subcategory subcategory);

	List<Product> findByStatus(Integer status);

	Page<Product> findByStatus(Integer status, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.status = 1 ORDER BY p.createAt DESC")
	List<Product> findActiveProductsOrderByNewest();

	// ====== Theo chủ sở hữu (ownerUser.userID) ======
	List<Product> findByOwnerUser_UserID(String ownerUserId);

	Page<Product> findByOwnerUser_UserID(String ownerUserId, Pageable pageable);

	Page<Product> findByOwnerUser_UserIDAndStatus(String ownerUserId, Integer status, Pageable pageable);

	Optional<Product> findByProductIDAndOwnerUser_UserID(String productId, String ownerUserId);

	long countByOwnerUser_UserID(String ownerUserId);

	boolean existsByProductIDAndOwnerUser_UserID(String productId, String ownerUserId);

	// ====== Search trong phạm vi owner (active) ======
	@Query("""
			SELECT p FROM Product p
			WHERE p.ownerUser.userID = :ownerUserId
			  AND p.status = 1
			  AND (
			    LOWER(p.name) LIKE CONCAT('%', LOWER(:kw), '%') OR
			    LOWER(p.brand.name) LIKE CONCAT('%', LOWER(:kw), '%') OR
			    LOWER(p.subcategory.name) LIKE CONCAT('%', LOWER(:kw), '%') OR
			    LOWER(p.subcategory.category.name) LIKE CONCAT('%', LOWER(:kw), '%') OR
			    p.description LIKE CONCAT('%', :kw, '%')
			  )
			ORDER BY p.createAt DESC
			""")
	Page<Product> searchMyActiveProducts(@Param("ownerUserId") String ownerUserId, @Param("kw") String keyword,
			Pageable pageable);
	
	
}
