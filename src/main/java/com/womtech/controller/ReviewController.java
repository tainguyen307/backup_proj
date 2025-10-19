package com.womtech.controller;

import com.womtech.entity.Review;
import com.womtech.entity.Product;
import com.womtech.entity.User;
import com.womtech.service.ProductService;
import com.womtech.service.ReviewService;
import com.womtech.service.UserService;
import com.womtech.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;
    private final UserService userService;
    private final OrderItemService orderItemService;

    // 🟢 Gửi đánh giá
    @PostMapping("/add")
    public String addReview(
            @RequestParam("productID") String productID,
            @RequestParam("rating") int rating,
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

        if (comment == null || comment.trim().length() < 50) {
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
        String userID = auth.getName();

        Optional<Review> reviewOpt = reviewService.findById(id);
        if (reviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá cần xóa.");
            return "redirect:/products";
        }

        Review review = reviewOpt.get();
        String productID = review.getProduct().getProductID();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ADMIN"));

        if (review.getUser().getUserID().equals(userID) || isAdmin) {
            reviewService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa đánh giá này.");
        }

        return "redirect:/product/" + productID;
    }
}
