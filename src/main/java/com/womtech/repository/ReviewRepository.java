package com.womtech.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.womtech.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
	Page<Review> findByProduct_ProductIDAndStatus(String productId, Integer status, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productID = :pid AND r.status = 1")
    Double avgRatingByProduct(@Param("pid") String productId);
}