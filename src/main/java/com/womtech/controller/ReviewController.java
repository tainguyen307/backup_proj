package com.womtech.controller;

import com.womtech.entity.Review;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.womtech.entity.Product;
import com.womtech.entity.User;
import com.womtech.service.ProductService;
import com.womtech.service.ReviewService;
import com.womtech.service.UserService;
import com.womtech.service.CloudinaryService;
import com.womtech.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final UserService userService;
    private final OrderItemService orderItemService;
    private final CloudinaryService cloudinaryService;
    private final Cloudinary cloudinary;

    // 🟢 Gửi đánh giá
    @PostMapping("/add")
    public String addReview(
            @RequestParam("productID") String productID,
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam("comment") String comment,
            RedirectAttributes redirectAttributes
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userID = auth.getName();

        Optional<Product> productOpt = productService.getProductById(productID);
        Optional<User> userOpt = userService.findById(userID);

        if (productOpt.isEmpty() || userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại hoặc tài khoản không hợp lệ.");
            return "redirect:/product/" + productID;
        }

        boolean hasPurchased = orderItemService.hasUserPurchasedProduct(userID, productID);
        if (!hasPurchased) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa mua sản phẩm này nên không thể đánh giá.");
            return "redirect:/product/" + productID;
        }
        
        if (rating == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn số sao đánh giá hợp lệ (1–5).");
            return "redirect:/product/" + productID;
        }
        
        String plainText = Jsoup.parse(comment).text();
        
        if (plainText == null || plainText.trim().length() < 50) {
            redirectAttributes.addFlashAttribute("error", "Bình luận phải có ít nhất 50 ký tự.");
            return "redirect:/product/" + productID;
        }

        Review review = Review.builder()
                .product(productOpt.get())
                .user(userOpt.get())
                .rating(rating)
                .comment(comment)
                .status(1)
                .build();

        reviewService.save(review);
        redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã gửi đánh giá!");
        return "redirect:/product/" + productID;
    }

    // 🟡 Cập nhật đánh giá
    @PostMapping("/update/{id}")
    public String updateReview(
            @PathVariable String id,
            @RequestParam("comment") String comment,
            @RequestParam("rating") Integer rating,
            RedirectAttributes redirectAttributes
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userID = auth.getName();

        Optional<Review> reviewOpt = reviewService.findById(id);
        if (reviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá.");
            return "redirect:/products";
        }

        Review review = reviewOpt.get();
        String productID = review.getProduct().getProductID();

        if (!review.getUser().getUserID().equals(userID)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa đánh giá này.");
            return "redirect:/product/" + productID;
        }

        String plainText = Jsoup.parse(comment).text();
        
        if (plainText == null || plainText.trim().length() < 50) {
            redirectAttributes.addFlashAttribute("error", "Bình luận phải có ít nhất 50 ký tự.");
            return "redirect:/product/" + productID;
        }
        
        Set<String> oldImages = reviewService.extractImageUrls(review.getComment());
        Set<String> newImages = reviewService.extractImageUrls(comment);
        oldImages.stream()
        	.filter(img -> !newImages.contains(img))
        	.forEach(img -> cloudinaryService.deleteImage(img));
        
        review.setComment(comment);
        review.setRating(rating);
        reviewService.save(review);

        redirectAttributes.addFlashAttribute("success", "Cập nhật đánh giá thành công!");
        return "redirect:/product/" + productID;
    }

    // 🔴 Xóa đánh giá
    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<Review> reviewOpt = reviewService.findById(id);
        if (reviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá cần xóa.");
            return "redirect:/products";
        }

        Review review = reviewOpt.get();
        String productID = review.getProduct().getProductID();

        System.out.println("Review username: " + review.getUser().getUsername());
        System.out.println("Review userID: " + review.getUser().getUserID());

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (review.getUser().getUsername().equals(username) || isAdmin) {
        	reviewService.extractImageUrls(review.getComment())
            	.forEach(img -> cloudinaryService.deleteImage(img));
            reviewService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa đánh giá này.");
        }

        return "redirect:/product/" + productID;
    }
    
    @PostMapping("/upload-image")
    @ResponseBody
    public Map<String, Object> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Tự upload trực tiếp bằng Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "womtech/reviews",
                    "resource_type", "image"
            ));

            String url = uploadResult.get("secure_url").toString();
            result.put("success", 1);
            result.put("url", url);

        } catch (Exception e) {
            result.put("success", 0);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
