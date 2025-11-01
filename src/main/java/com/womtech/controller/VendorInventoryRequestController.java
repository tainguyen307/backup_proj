package com.womtech.controller;

import com.womtech.entity.InventoryRequest;
import com.womtech.entity.Product;
import com.womtech.entity.User;
import com.womtech.repository.ProductRepository;
import com.womtech.service.InventoryRequestService;
import com.womtech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/vendor/inventory-requests")
public class VendorInventoryRequestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private InventoryRequestService inventoryRequestService;

    // ================== HELPER ==================
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String userId = authentication.getName();
        return userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ================== HIỂN THỊ FORM ==================
    @GetMapping("/create")
    public String showRequestForm(Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // Lấy danh sách sản phẩm của vendor hiện tại
        List<Product> products = productRepository.findByOwnerUser(currentUser);

        model.addAttribute("products", products);
        model.addAttribute("inventoryRequest", new InventoryRequest());
        return "vendor/inventory-request-form";
    }

    // ================== SUBMIT FORM ==================
    @PostMapping("/create")
    public String submitRequest(@RequestParam("productId") String productId,
                                @RequestParam("quantity") Integer quantity,
                                @RequestParam(value = "note", required = false) String note,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser(authentication);

            // Lấy sản phẩm theo ID
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

            // Kiểm tra quyền sở hữu sản phẩm
            if (!product.getOwnerUser().getUserID().equals(currentUser.getUserID())) {
                throw new IllegalArgumentException("Bạn không có quyền tạo yêu cầu cho sản phẩm này");
            }

            // Tạo yêu cầu nhập hàng
            inventoryRequestService.createRequest(product, quantity, note, currentUser);

            redirectAttributes.addFlashAttribute("success", "Yêu cầu nhập hàng đã được gửi thành công!");
            return "redirect:/vendor/inventory-requests";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/inventory-requests/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/vendor/inventory-requests/create";
        }
    }

    // ================== DANH SÁCH YÊU CẦU ==================
    @GetMapping
    public String listRequests(Model model, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        List<InventoryRequest> requests = inventoryRequestService.getRequestsByUser(currentUser);
        long pendingCount = inventoryRequestService.countUserPendingRequests(currentUser);

        model.addAttribute("requests", requests);
        model.addAttribute("pendingCount", pendingCount);
        return "vendor/inventory-request-list";
    }
}
