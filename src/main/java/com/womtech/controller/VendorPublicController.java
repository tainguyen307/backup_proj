package com.womtech.controller;

import com.womtech.entity.User;
import com.womtech.service.UserService;
import com.womtech.util.CookieUtil;
import com.womtech.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("")
public class VendorPublicController {

    private final EmailUtil emailUtil;
    private final UserService userService;

    public VendorPublicController(EmailUtil emailUtil, UserService userService) {
        this.emailUtil = emailUtil;
        this.userService = userService;
    }

    // Cookie names
    private static final String CK_AT = "AT";
    private static final String CK_RT = "RT";
    private static final String CK_REMEMBER = "WOM_REMEMBER";

    // Session keys cho flow “Vendor Register”
    private static final String SK_VR_OTP = "VR_OTP";
    private static final String SK_VR_EXPIRE = "VR_EXPIRE";
    private static final String SK_VR_LAST_SEND = "VR_LAST_SEND";

    private static final int VR_OTP_TTL_MIN = 10;
    private static final int VR_RESEND_COOLDOWN = 60;

    // GET /vendor-register
    @GetMapping("/vendor-register")
    public String showVendorRegisterOtp(Authentication authentication, Model model, RedirectAttributes ra,
                                        HttpServletRequest req) {

        User currentUser = getCurrentUser(authentication);

        List<String> roles = userService.getRolesByUserId(currentUser.getUserID());
        boolean alreadyVendor = roles.stream().anyMatch(r -> "VENDOR".equalsIgnoreCase(r));
        if (alreadyVendor) {
            ra.addFlashAttribute("info", "Tài khoản của bạn đã là Vendor.");
            return "redirect:/";
        }

        String email = currentUser.getEmail();
        if (email == null || email.isBlank()) {
            ra.addFlashAttribute("error", "Tài khoản chưa có email để nhận OTP. Vui lòng cập nhật email trước.");
            return "redirect:/user/profile";
        }

        HttpSession session = req.getSession(true);

        Instant last = (Instant) session.getAttribute(SK_VR_LAST_SEND);
        if (last != null && Duration.between(last, Instant.now()).getSeconds() < VR_RESEND_COOLDOWN) {
            long left = VR_RESEND_COOLDOWN - Duration.between(last, Instant.now()).getSeconds();
            ra.addFlashAttribute("error", "Vui lòng chờ " + left + " giây nữa để gửi lại OTP.");
            return "redirect:/vendor-register";
        }

        String otp = generateOtp();
        Instant expire = Instant.now().plus(Duration.ofMinutes(VR_OTP_TTL_MIN));
        session.setAttribute(SK_VR_OTP, otp);
        session.setAttribute(SK_VR_EXPIRE, expire);
        session.setAttribute(SK_VR_LAST_SEND, Instant.now());

        try {
            emailUtil.sendVerifyOtp(email, currentUser.getUsername(), otp, VR_OTP_TTL_MIN);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể gửi email OTP. Vui lòng thử lại.");
            clearVrSession(session);
            return "redirect:/";
        }

        model.addAttribute("maskedEmail", maskEmail(email));
        model.addAttribute("ttlSeconds", VR_OTP_TTL_MIN * 60);
        model.addAttribute("cooldown", VR_RESEND_COOLDOWN);
        return "vendor/register-otp";
    }

    // POST /vendor-register
    @PostMapping("/vendor-register")
    public String handleVendorRegisterOtp(@RequestParam("otp") String otp, Authentication authentication,
                                          HttpServletRequest request, HttpServletResponse response,
                                          RedirectAttributes ra) {
        User currentUser = getCurrentUser(authentication);
        HttpSession session = request.getSession(false);
        if (session == null) {
            ra.addFlashAttribute("error", "Phiên OTP không hợp lệ. Vui lòng thử lại.");
            return "redirect:/vendor-register";
        }

        String saved = (String) session.getAttribute(SK_VR_OTP);
        Instant expireAt = (Instant) session.getAttribute(SK_VR_EXPIRE);

        if (saved == null || expireAt == null) {
            ra.addFlashAttribute("error", "OTP không tồn tại hoặc phiên đã hết hạn.");
            clearVrSession(session);
            return "redirect:/vendor-register";
        }
        if (Instant.now().isAfter(expireAt)) {
            ra.addFlashAttribute("error", "OTP đã hết hạn. Vui lòng gửi lại.");
            clearVrSession(session);
            return "redirect:/vendor-register";
        }
        if (otp == null || !otp.trim().equals(saved)) {
            ra.addFlashAttribute("error", "OTP không đúng. Vui lòng kiểm tra lại.");
            return "redirect:/vendor-register";
        }

        try {
            userService.promoteToVendor(currentUser.getUserID());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể cập nhật quyền Vendor: " + e.getMessage());
            return "redirect:/vendor-register";
        } finally {
            clearVrSession(session);
        }

        // Ép đăng nhập lại
        clearAuthCookies(request, response);
        session.invalidate();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        ra.addFlashAttribute("success", "Đăng ký shop thành công! Vui lòng đăng nhập lại để áp dụng quyền Vendor.");
        return "redirect:/auth/login";
    }

    // POST /vendor-register/resend
    @PostMapping("/vendor-register/resend")
    public String resendVendorOtp(Authentication authentication, HttpServletRequest req, RedirectAttributes ra) {
        User currentUser = getCurrentUser(authentication);
        HttpSession session = req.getSession(true);

        Instant last = (Instant) session.getAttribute(SK_VR_LAST_SEND);
        if (last != null && Duration.between(last, Instant.now()).getSeconds() < VR_RESEND_COOLDOWN) {
            long left = VR_RESEND_COOLDOWN - Duration.between(last, Instant.now()).getSeconds();
            ra.addFlashAttribute("error", "Vui lòng chờ " + left + " giây nữa để gửi lại OTP.");
            return "redirect:/vendor-register";
        }

        String otp = generateOtp();
        Instant expire = Instant.now().plus(Duration.ofMinutes(VR_OTP_TTL_MIN));
        session.setAttribute(SK_VR_OTP, otp);
        session.setAttribute(SK_VR_EXPIRE, expire);
        session.setAttribute(SK_VR_LAST_SEND, Instant.now());

        try {
            emailUtil.sendVerifyOtp(currentUser.getEmail(), currentUser.getUsername(), otp, VR_OTP_TTL_MIN);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Không thể gửi lại OTP lúc này. Vui lòng thử lại.");
            return "redirect:/vendor-register";
        }

        ra.addFlashAttribute("success", "Đã gửi lại OTP đến email của bạn.");
        return "redirect:/vendor-register";
    }

    // ===== Helpers =====
    private static String generateOtp() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "email của bạn";
        String[] parts = email.split("@", 2);
        String local = parts[0];
        if (local.length() <= 2) return local.charAt(0) + "***@" + parts[1];
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + parts[1];
    }
    private static void clearVrSession(HttpSession s) {
        s.removeAttribute(SK_VR_OTP);
        s.removeAttribute(SK_VR_EXPIRE);
        s.removeAttribute(SK_VR_LAST_SEND);
    }
    private static void clearAuthCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.delete(request, response, CK_AT);
        CookieUtil.delete(request, response, CK_RT);
        CookieUtil.delete(request, response, CK_REMEMBER);

        var js = new jakarta.servlet.http.Cookie("JSESSIONID", "");
        js.setPath("/");
        js.setHttpOnly(true);
        js.setMaxAge(0);
        response.addCookie(js);
        response.addHeader("Set-Cookie","JSESSIONID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw new RuntimeException("User not authenticated");
        String userID = authentication.getName();
        return userService.findById(userID).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
