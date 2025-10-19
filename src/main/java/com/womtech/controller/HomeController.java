package com.womtech.controller;

import com.womtech.service.CategoryService;
import com.womtech.service.PostService;
import com.womtech.service.ProductService;
import com.womtech.service.UserService;
import com.womtech.util.CookieUtil;
import com.womtech.entity.Category;
import com.womtech.entity.Post;
import com.womtech.entity.Product;
import com.womtech.entity.Review;
import com.womtech.security.JwtService;
import com.womtech.security.TokenRevokeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@ControllerAdvice
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final PostService postService;
    private final JwtService jwtService;
    private final TokenRevokeService tokenRevokeService;

    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Principal principal
    ) {
        int pageSize = (principal != null) ? 20 : 8;

        Pageable productPageable = PageRequest.of(page, pageSize, Sort.by("createAt").descending());
        Page<Product> productPage = productService.getActiveProducts(productPageable);
        List<Category> categories = categoryService.findAll();
        Map<Category, List<Product>> productsByCategory = new LinkedHashMap<>();
        for (var cate : categories) {
            List<Product> cateProducts = productService
                    .getActiveProductsByCategory(cate.getCategoryID(), PageRequest.of(0, 8))
                    .getContent();
            productsByCategory.put(cate, cateProducts);
        }

        Pageable postPageable = PageRequest.of(0, 8, Sort.by("createAt").descending());
        List<Post> latestPosts = postService.getAllActive(postPageable).getContent();

        model.addAttribute("featuredProducts", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("featuredCategories", categories);
        model.addAttribute("productsByCategory", productsByCategory);
        model.addAttribute("latestPosts", latestPosts);

        return "index";
    }
    @ModelAttribute
    public void addAuthenticationInfo(Model model, Principal principal, HttpServletRequest request, HttpSession session) {
        boolean isAuthenticated = false;
        String currentUserId = null;
        String currentUsername = null;
        String currentRole = null;

        System.out.println("=== DEBUG AUTHENTICATION ===");
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
        
        var cookie = CookieUtil.get(request, "AT");
        System.out.println("AT Cookie: " + (cookie != null ? "exists" : "null"));
        if (cookie != null) {
            System.out.println("AT Valid: " + jwtService.isValidAccess(cookie.getValue()));
            boolean isRevoked = tokenRevokeService.isRevoked(cookie.getValue());
            System.out.println("AT Revoked: " + isRevoked);
            System.out.println("AT Username: " + jwtService.getUsername(cookie.getValue()));
            System.out.println("AT Token: " + cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "...");
        }
        
        Object sessionUserId = session.getAttribute("CURRENT_USER_ID");
        System.out.println("Session User ID: " + sessionUserId);
        System.out.println("=============================");

        // Ưu tiên JWT authentication (Principal)
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            isAuthenticated = true;
            currentUserId = principal.getName();
            
            // Lấy username từ JWT token
            if (cookie != null) {
                currentUsername = jwtService.getUsername(cookie.getValue());
            }
            
            // Fallback: lấy từ database nếu không có trong token
            if (currentUsername == null) {
                userService.findById(currentUserId).ifPresent(user -> {
                    model.addAttribute("currentUsername", user.getUsername());
                });
            }
            if (currentUserId != null) {
                var userOpt = userService.findById(currentUserId);
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    currentUsername = user.getUsername();
                    currentRole = user.getRole().getRolename();
                }
            }
        } else if (cookie != null && jwtService.isValidAccess(cookie.getValue()) && !tokenRevokeService.isRevoked(cookie.getValue())) {
            // Nếu Principal null nhưng có valid JWT token và chưa bị revoke -> vẫn authenticated
            isAuthenticated = true;
            currentUserId = jwtService.getUserId(cookie.getValue());
            currentUsername = jwtService.getUsername(cookie.getValue());
            
            if (currentUserId != null) {
                var userOpt = userService.findById(currentUserId);
                if (userOpt.isPresent()) {
                    currentRole = userOpt.get().getRole().getRolename();
                }
            }
        } else {
            // Nếu có cookie nhưng token bị revoke -> xóa cookies
            if (cookie != null && tokenRevokeService.isRevoked(cookie.getValue())) {
                clearAuthCookies(request);
            }
            
            // KHÔNG fallback về session để tránh hiển thị user sau khi logout
            // Chỉ dựa vào JWT authentication
        }

        System.out.println("Final result - isAuthenticated: " + isAuthenticated + ", username: " + currentUsername);

        // Load featured products (lấy 8 sản phẩm đầu tiên)
        model.addAttribute("featuredProducts", productService.getAllProducts().stream().limit(10).toList());
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("currentRole", currentRole);
    }

    private void clearAuthCookies(HttpServletRequest request) {
        // Xóa cookies bằng cách set response headers
        // Note: Trong @ModelAttribute, chúng ta không có HttpServletResponse
        // Nên sẽ xóa cookies ở client-side hoặc trong filter
        System.out.println("Token bị revoke - cần xóa cookies");
    }
    
    @GetMapping("/about")
    public String about() {
        return "user/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "user/contact";
    }
    
    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword,
                                 @RequestParam(defaultValue = "0") int page,
                                 Model model) {

        int pageSize = 8;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> products = productService.searchActiveProducts(keyword, pageable);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalElements", products.getTotalElements());

        return "user/search";
    }
    
    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String ids,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int pageSize = 20;

        // Nếu sort là "favorite" (danh sách yêu thích)
        if ("favorite".equals(sort)) {
            if (ids != null && !ids.isEmpty()) {
                // Tách danh sách id từ query ?ids=1,2,3
                List<String> idList = Arrays.asList(ids.split(","));

                // Lọc sản phẩm
                List<Product> favList = productService.getAllProducts()
                        .stream()
                        .filter(p -> idList.contains(p.getProductID()))
                        .toList();

                // Tính phân trang thủ công
                int start = Math.min(page * pageSize, favList.size());
                int end = Math.min(start + pageSize, favList.size());
                List<Product> pageContent = favList.subList(start, end);

                // Tạo đối tượng Page giả lập
                Page<Product> favPage = new PageImpl<>(
                    pageContent,
                    PageRequest.of(page, pageSize),
                    favList.size()
                );

                // Gửi sang view
                model.addAttribute("products", favPage);
                model.addAttribute("selectedSort", sort);
                model.addAttribute("selectedCategory", category);
                model.addAttribute("categories", categoryService.findAll());

                return "user/products";
            }
        }

        // Sắp xếp mặc định
        Sort sortOption = switch (sort != null ? sort : "") {
            case "priceAsc" -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "latest" -> Sort.by("createAt").descending();
            default -> Sort.by("name").ascending();
        };

        Pageable pageable = PageRequest.of(page, pageSize, sortOption);
        Page<Product> products;

        if (category != null && !category.isBlank()) {
            products = productService.getActiveProductsByCategory(category, pageable);
        } else {
            products = productService.getActiveProducts(pageable);
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSort", sort);

        return "user/products";
    }
    
    @GetMapping("/product/{id}")
    public String productDetail(
            @PathVariable("id") String productId,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Principal principal) {

        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }

        Product product = productOpt.get();

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createAt").descending());
        Page<Review> reviewPage = productService.getReviews(productId, pageable);

        double avgRating = reviewPage.getContent().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);

        String currentUserId = principal != null ? principal.getName() : null;

        model.addAttribute("product", product);
        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("avgRating", (int) Math.round(avgRating));
        model.addAttribute("currentUserId", currentUserId);

        List<Product> relatedProducts = productService.getActiveProducts().stream()
                .filter(p -> p.getSubcategory() != null
                        && p.getSubcategory().equals(product.getSubcategory())
                        && !p.getProductID().equals(productId))
                .limit(8)
                .toList();
        model.addAttribute("relatedProducts", relatedProducts);

        return "user/product-detail";
    }
    
    @GetMapping("/posts")
    public String allPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        int pageSize = 20;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<Post> postPage = postService.getAllActive(pageable);

        model.addAttribute("postPage", postPage);
        model.addAttribute("latestPosts", postPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());

        return "user/posts";
    }
    
    @GetMapping("/post/{id}")
    public String postDetail(
            @PathVariable("id") String postId,
            Model model,
            Principal principal
    ) {
        Optional<Post> postOpt = postService.findById(postId);
        if (postOpt.isEmpty()) {
            return "redirect:/posts";
        }

        Post post = postOpt.get();
        String currentUserId = principal != null ? principal.getName() : null;

        model.addAttribute("post", post);
        model.addAttribute("currentUserId", currentUserId);

        return "user/post-detail";
    }
}
