package com.womtech.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import com.womtech.entity.Review;
import com.womtech.repository.ReviewRepository;
import com.womtech.service.ReviewService;

@Service
public class ReviewServiceImpl extends BaseServiceImpl<Review, String> implements ReviewService {

    private final ReviewRepository reviewRepository;
    
    protected ReviewServiceImpl(ReviewRepository reviewRepository) {
        super(reviewRepository);
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Page<Review> getReviews(String productId, Pageable pageable) {
        return reviewRepository.findByProduct_ProductIDAndStatus(productId, 1, pageable);
    }

    @Override
    public Double avgRatingByProduct(String productId) {
        return reviewRepository.avgRatingByProduct(productId);
    }
    
    @Override
    public Set<String> extractImageUrls(String html) {
        Set<String> urls = new HashSet<>();
        if (html == null || html.isEmpty()) return urls;

        Document doc = Jsoup.parse(html);
        for (Element img : doc.select("img")) {
            String src = img.attr("src");
            if (!src.isEmpty()) urls.add(src);
        }

        return urls;
    }
}