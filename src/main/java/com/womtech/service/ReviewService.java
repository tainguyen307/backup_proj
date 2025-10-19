package com.womtech.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.womtech.entity.Review;

public interface ReviewService extends BaseService<Review, String> {
	Page<Review> getReviews(String productId, Pageable pageable);
    Double avgRatingByProduct(String productId);
}