package com.womtech.controller;

import com.womtech.dto.request.auth.LoginRequest;
import com.womtech.dto.request.auth.RegisterRequest;
import com.womtech.dto.response.auth.LoginResponse;
import com.womtech.dto.response.auth.RegisterResponse;
import com.womtech.security.JwtService;
import com.womtech.security.TokenRevokeService;
import com.womtech.service.UserService;
import com.womtech.util.CookieUtil;
import com.womtech.util.EmailUtil;
import com.womtech.util.RememberMeUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final EmailUtil emailUtil;
	private final JwtService jwtService;
	private final TokenRevokeService tokenRevokeService;

	private static final String CK_REMEMBER = "WOM_REMEMBER";
	private static final int REMEMBER_TTL = RememberMeUtil.REMEMBER_ME_TTL;
	private static final String CK_AT = "AT";
	private static final String CK_RT = "RT";
	private static final int ACCESS_TTL_SEC = 15 * 60;
	private static final int REFRESH_TTL_SEC = 7 * 24 * 3600;

	private static final String SK_REG_PENDING = "REG_PENDING_REQ";
	private static final String SK_REG_EMAIL_MASK = "REG_EMAIL_MASK";
	private static final String SK_OTP_CODE = "REG_OTP_CODE";
	private static final String SK_OTP_EXPIRE = "REG_OTP_EXPIRE";
	private static final String SK_OTP_LAST_SENT = "REG_OTP_LAST_SENT";
	private static final int OTP_TTL_MINUTES = 10;
	private static final int RESEND_COOLDOWN_SECONDS = 60;

	private static final String SK_FP_EMAIL = "FP_EMAIL";
	private static final String SK_FP_OTP = "FP_OTP";
	private static final String SK_FP_EXPIRE = "FP_EXPIRE";
	private static final String SK_FP_ATTEMPTS = "FP_ATTEMPTS";
	private static final String SK_FP_LASTSEND = "FP_LASTSEND";
	private static final String SK_FP_VERIFIED = "FP_VERIFIED";

	private static final int FP_OTP_TTL_MIN = 15;
	private static final int FP_MAX_ATTEMPTS = 5;
	private static final long FP_RESEND_COOLDN = 60_000L;

	@GetMapping("/login")
	public String showLogin(HttpServletRequest request, HttpSession session) {
		if (session.getAttribute("CURRENT_USER_ID") != null) {
			return "redirect:/";
		}
		Cookie ck = CookieUtil.get(request, CK_REMEMBER);
		if (ck != null) {
			String userId = RememberMeUtil.verifyToken(ck.getValue());
			if (userId != null) {
				userService.findById(userId).ifPresent(u -> {
					HttpSession s = request.getSession(true);
					s.setAttribute("CURRENT_USER_ID", u.getUserID());
					s.setAttribute("CURRENT_USERNAME", u.getUsername());
					List<String> roles = userService.getRolesByUserId(u.getUserID());
					s.setAttribute("CURRENT_ROLES", roles);
				});
				return "redirect:/";
			}
		}
		return "auth/login";
	}

	@PostMapping("/login")
	public String doLogin(@RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam(value = "rememberMe", required = false) Boolean rememberMe, HttpSession session,
			HttpServletResponse response, RedirectAttributes ra, Model model) {
		if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
			model.addAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
			return "auth/login";
		}

		LoginResponse res = userService.login(LoginRequest.builder().username(username).password(password).build());

		if (!"Login successful!".equalsIgnoreCase(res.getMessage())) {
			model.addAttribute("error", res.getMessage());
			return "auth/login";
		}

		List<String> roles = userService.getRolesByUserId(res.getUserID());

		session.setAttribute("CURRENT_USER_ID", res.getUserID());
		session.setAttribute("CURRENT_USERNAME", res.getUsername());
		session.setAttribute("CURRENT_ROLES", roles);

		String at = jwtService.generateAccessToken(res.getUserID(), res.getUsername(), roles);
		String rt = jwtService.generateRefreshToken(res.getUserID());
		CookieUtil.add(response, CK_AT, at, ACCESS_TTL_SEC, true, false, "Lax");
		CookieUtil.add(response, CK_RT, rt, REFRESH_TTL_SEC, true, false, "Lax");

		if (Boolean.TRUE.equals(rememberMe)) {
			String token = RememberMeUtil.generateToken(res.getUserID());
			CookieUtil.add(response, CK_REMEMBER, token, REMEMBER_TTL, true, false, "Lax");
		}

		ra.addFlashAttribute("success", "Đăng nhập thành công!");
		String redirect = (res.getRedirectUrl() != null && !res.getRedirectUrl().isBlank()) ? res.getRedirectUrl()
				: resolveRedirectByRoles(roles);
		return "redirect:" + redirect;
	}

	@GetMapping("/register")
	public String showRegister() {
		return "auth/register";
	}

	@PostMapping("/register")
	public String doRegister(@RequestParam("email") String email, @RequestParam("username") String username,
			@RequestParam("password") String password, @RequestParam("confirmPassword") String confirmPassword,
			RedirectAttributes ra, Model model, HttpSession session) {

		if (!StringUtils.hasText(email) || !StringUtils.hasText(username) || !StringUtils.hasText(password)
				|| !StringUtils.hasText(confirmPassword)) {
			model.addAttribute("error", "Vui lòng nhập đầy đủ Email, Username, Mật khẩu và Xác nhận mật khẩu.");
			return "auth/register";
		}
		if (username.length() < 4) {
			model.addAttribute("error", "Tên đăng nhập phải có ít nhất 4 ký tự.");
			return "auth/register";
		}
		if (password.length() < 6) {
			model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
			return "auth/register";
		}
		if (!password.equals(confirmPassword)) {
			model.addAttribute("error", "Xác nhận mật khẩu không khớp.");
			return "auth/register";
		}
		if (userService.existsByEmail(email)) {
			model.addAttribute("error", "Email đã được đăng ký.");
			return "auth/register";
		}
		if (userService.existsByUsername(username)) {
			model.addAttribute("error", "Tên đăng nhập đã tồn tại.");
			return "auth/register";
		}

		RegisterRequest pending = RegisterRequest.builder().email(email).username(username).password(password).build();

		session.setAttribute(SK_REG_PENDING, pending);
		session.setAttribute(SK_REG_EMAIL_MASK, maskEmail(email));

		String otp = generateOtp();
		Instant expireAt = Instant.now().plus(Duration.ofMinutes(OTP_TTL_MINUTES));
		session.setAttribute(SK_OTP_CODE, otp);
		session.setAttribute(SK_OTP_EXPIRE, expireAt);
		session.setAttribute(SK_OTP_LAST_SENT, Instant.now());

		try {
			emailUtil.sendVerifyOtp(email, username, otp, OTP_TTL_MINUTES);
		} catch (Exception e) {
			clearOtpSession(session);
			model.addAttribute("error", "Không thể gửi email OTP. Vui lòng thử lại.");
			return "auth/register";
		}

		ra.addFlashAttribute("success", "Mã OTP đã được gửi tới email của bạn.");
		return "redirect:/auth/verify-otp";
	}

	@GetMapping("/verify-otp")
	public String showVerifyOtp(Model model, HttpSession session, RedirectAttributes ra) {
		RegisterRequest pending = (RegisterRequest) session.getAttribute(SK_REG_PENDING);
		if (pending == null) {
			ra.addFlashAttribute("error", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
			return "redirect:/auth/register";
		}

		Instant exp = (Instant) session.getAttribute(SK_OTP_EXPIRE);
		long remain = Math.max(0,
				Duration.between(Instant.now(), Objects.requireNonNullElse(exp, Instant.now())).getSeconds());
		model.addAttribute("maskedEmail", (String) session.getAttribute(SK_REG_EMAIL_MASK));
		model.addAttribute("remainingSeconds", Math.min(remain, RESEND_COOLDOWN_SECONDS));
		return "auth/verify-otp";
	}

	@PostMapping("/verify-otp")
	public String doVerifyOtp(@RequestParam("otp") String otp, HttpSession session, RedirectAttributes ra, HttpServletResponse response) {
		RegisterRequest pending = (RegisterRequest) session.getAttribute(SK_REG_PENDING);
		String code = (String) session.getAttribute(SK_OTP_CODE);
		Instant expireAt = (Instant) session.getAttribute(SK_OTP_EXPIRE);

		if (pending == null || code == null || expireAt == null) {
			ra.addFlashAttribute("error", "Phiên OTP không hợp lệ hoặc đã hết hạn.");
			return "redirect:/auth/register";
		}
		if (Instant.now().isAfter(expireAt)) {
			ra.addFlashAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại mã.");
			return "redirect:/auth/verify-otp";
		}
		if (!otp.equals(code)) {
			ra.addFlashAttribute("error", "Mã OTP không đúng. Vui lòng kiểm tra lại.");
			return "redirect:/auth/verify-otp";
		}

		RegisterResponse res = userService.register(pending);
		if (!"Register successful!".equalsIgnoreCase(res.getMessage())) {
			ra.addFlashAttribute("error", "Không thể tạo tài khoản: " + res.getMessage());
			return "redirect:/auth/register";
		}

		String userId = res.getUserID();
		String username = res.getUsername();

		if (userId == null || username == null) {
			userService.findByEmail(pending.getEmail()).ifPresent(u -> {
				session.setAttribute("CURRENT_USER_ID", u.getUserID());
				session.setAttribute("CURRENT_USERNAME", u.getUsername());
				session.setAttribute("CURRENT_ROLES", userService.getRolesByUserId(u.getUserID()));
			});
		} else {
			session.setAttribute("CURRENT_USER_ID", userId);
			session.setAttribute("CURRENT_USERNAME", username);
			session.setAttribute("CURRENT_ROLES", userService.getRolesByUserId(userId));
			// Tạo JWT cookie ngay sau khi verify OTP thành công
			String at = jwtService.generateAccessToken(userId, username, userService.getRolesByUserId(userId));
			String rt = jwtService.generateRefreshToken(userId);
			// Xác định secure dựa vào host
			CookieUtil.add(response, CK_AT, at, ACCESS_TTL_SEC, true, false, "Lax");
			CookieUtil.add(response, CK_RT, rt, REFRESH_TTL_SEC, true, false, "Lax");
		}

		clearOtpSession(session);
		ra.addFlashAttribute("success", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
		return "redirect:/";
	}

	@PostMapping("/resend-otp")
	public String resendOtp(HttpSession session, RedirectAttributes ra) {
		RegisterRequest pending = (RegisterRequest) session.getAttribute(SK_REG_PENDING);
		Instant last = (Instant) session.getAttribute(SK_OTP_LAST_SENT);
		if (pending == null) {
			ra.addFlashAttribute("error", "Phiên đăng ký đã hết hạn.");
			return "redirect:/auth/register";
		}
		if (last != null) {
			long diff = Duration.between(last, Instant.now()).getSeconds();
			if (diff < RESEND_COOLDOWN_SECONDS) {
				ra.addFlashAttribute("error",
						"Vui lòng chờ " + (RESEND_COOLDOWN_SECONDS - diff) + " giây để gửi lại OTP.");
				return "redirect:/auth/verify-otp";
			}
		}

		String otp = generateOtp();
		Instant exp = Instant.now().plus(Duration.ofMinutes(OTP_TTL_MINUTES));
		session.setAttribute(SK_OTP_CODE, otp);
		session.setAttribute(SK_OTP_EXPIRE, exp);
		session.setAttribute(SK_OTP_LAST_SENT, Instant.now());

		try {
			emailUtil.sendVerifyOtp(pending.getEmail(), pending.getUsername(), otp, OTP_TTL_MINUTES);
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Không thể gửi lại OTP. Vui lòng thử sau.");
			return "redirect:/auth/verify-otp";
		}

		ra.addFlashAttribute("success", "Đã gửi lại mã OTP.");
		return "redirect:/auth/verify-otp";
	}

	@PostMapping("/refresh-token")
	@ResponseBody
	public Object refreshToken(HttpServletRequest request, HttpServletResponse response) {
		Cookie rtCk = CookieUtil.get(request, CK_RT);
		if (rtCk == null)
			return Map.of("ok", false, "message", "Missing refresh token");

		String rt = rtCk.getValue();
		if (!jwtService.isValidRefresh(rt) || tokenRevokeService.isRevoked(rt)) {
			return Map.of("ok", false, "message", "Refresh token invalid or revoked");
		}

		String userId = jwtService.getUserId(rt);
		var userOpt = userService.findById(userId);
		if (userOpt.isEmpty())
			return Map.of("ok", false, "message", "User not found");

		List<String> roles = userService.getRolesByUserId(userId);
		String newAT = jwtService.generateAccessToken(userId, userOpt.get().getUsername(), roles);
		CookieUtil.add(response, CK_AT, newAT, ACCESS_TTL_SEC, true, false, "Strict");

		return Map.of("ok", true);
	}

	@GetMapping("/me")
	@ResponseBody
	public Object me(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			Principal principal) {
		Map<String, Object> debug = new HashMap<>();

		Cookie at = CookieUtil.get(request, CK_AT);
		if (at != null) {
			debug.put("hasATCookie", true);
			debug.put("atValue", at.getValue().substring(0, Math.min(20, at.getValue().length())) + "...");
			debug.put("atValid", jwtService.isValidAccess(at.getValue()));
			debug.put("atRevoked", tokenRevokeService.isRevoked(at.getValue()));
		} else {
			debug.put("hasATCookie", false);
		}

		if (principal != null) {
			debug.put("hasPrincipal", true);
			debug.put("principalName", principal.getName());
		} else {
			debug.put("hasPrincipal", false);
		}

		Object sessionUserId = session.getAttribute("CURRENT_USER_ID");
		if (sessionUserId != null) {
			debug.put("hasSession", true);
			debug.put("sessionUserId", sessionUserId);
		} else {
			debug.put("hasSession", false);
		}

		if (at != null && jwtService.isValidAccess(at.getValue()) && !tokenRevokeService.isRevoked(at.getValue())) {
			String userId = jwtService.getUserId(at.getValue());
			var userOpt = userService.findById(userId);
			if (userOpt.isEmpty()) {
				clearAuthCookies(request, response);
				return Map.of("ok", false, "message", "User not found", "debug", debug);
			}

			var u = userOpt.get();
			List<String> tokenRoles = jwtService.getRoles(at.getValue());
			return Map.of("ok", true, "by", "jwt", "userID", u.getUserID(), "username", u.getUsername(), "roles",
					tokenRoles, "debug", debug);
		}
		if (session.getAttribute("CURRENT_USER_ID") != null) {
			Object roles = session.getAttribute("CURRENT_ROLES");
			return Map.of("ok", true, "by", "session", "userID", session.getAttribute("CURRENT_USER_ID"), "username",
					session.getAttribute("CURRENT_USERNAME"), "roles", roles, "debug", debug);
		}

		if (at != null) {
			clearAuthCookies(request, response);
		}

		return Map.of("ok", false, "message", "No auth", "debug", debug);
	}

	@PostMapping("/manual-logout")
	public String doLogout(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			RedirectAttributes ra) {
		Cookie at = CookieUtil.get(request, CK_AT);
		Cookie rt = CookieUtil.get(request, CK_RT);

		System.out.println("=== DEBUG LOGOUT ===");
		if (at != null) {
			System.out.println(
					"Revoking AT token: " + at.getValue().substring(0, Math.min(20, at.getValue().length())) + "...");
			tokenRevokeService.revoke(at.getValue(), jwtService.getExpiry(at.getValue()));
			System.out.println("AT token revoked: " + tokenRevokeService.isRevoked(at.getValue()));
		}
		if (rt != null) {
			System.out.println(
					"Revoking RT token: " + rt.getValue().substring(0, Math.min(20, rt.getValue().length())) + "...");
			tokenRevokeService.revoke(rt.getValue(), jwtService.getExpiry(rt.getValue()));
		}
		System.out.println("===================");

		clearAuthCookies(request, response);
		session.invalidate();
		org.springframework.security.core.context.SecurityContextHolder.clearContext();

		ra.addFlashAttribute("success", "Bạn đã đăng xuất.");
		return "redirect:/auth/login";
	}

	@GetMapping("/manual-logout")
	public String doLogoutGet(HttpServletRequest request, HttpServletResponse response, HttpSession session,
			RedirectAttributes ra) {
		Cookie at = CookieUtil.get(request, CK_AT);
		Cookie rt = CookieUtil.get(request, CK_RT);

		System.out.println("=== DEBUG LOGOUT GET ===");
		if (at != null) {
			System.out.println(
					"Revoking AT token: " + at.getValue().substring(0, Math.min(20, at.getValue().length())) + "...");
			tokenRevokeService.revoke(at.getValue(), jwtService.getExpiry(at.getValue()));
			System.out.println("AT token revoked: " + tokenRevokeService.isRevoked(at.getValue()));
		}
		if (rt != null) {
			System.out.println(
					"Revoking RT token: " + rt.getValue().substring(0, Math.min(20, rt.getValue().length())) + "...");
			tokenRevokeService.revoke(rt.getValue(), jwtService.getExpiry(rt.getValue()));
		}
		System.out.println("=======================");

		clearAuthCookies(request, response);
		session.invalidate();
		org.springframework.security.core.context.SecurityContextHolder.clearContext();

		ra.addFlashAttribute("success", "Bạn đã đăng xuất.");
		return "redirect:/auth/login";
	}

	@GetMapping("/forgot")
	public String forgotForm() {
		return "auth/forgot";
	}

	@PostMapping("/forgot")
	public String forgotHandle(@RequestParam("email") String email, HttpServletRequest req, RedirectAttributes ra) {
		if (!StringUtils.hasText(email)) {
			ra.addFlashAttribute("error", "Vui lòng nhập email đã đăng ký.");
			return "redirect:/auth/forgot";
		}

		var userOpt = userService.findByEmail(email.trim());

		if (userOpt.isEmpty()) {
			// Tránh lộ thông tin tồn tại email
			ra.addFlashAttribute("info", "Nếu email hợp lệ, mã OTP sẽ được gửi trong giây lát.");
			return "redirect:/auth/forgot";
		}

		HttpSession session = req.getSession(true);
		Long last = (Long) session.getAttribute(SK_FP_LASTSEND);
		long now = System.currentTimeMillis();

		if (last != null && now - last < FP_RESEND_COOLDN) {
			ra.addFlashAttribute("error", "Vui lòng đợi 1 phút trước khi gửi lại OTP.");
			return "redirect:/auth/forgot";
		}

		String otp = generateOtp();
		long expireAt = now + FP_OTP_TTL_MIN * 60_000L;

		session.setAttribute(SK_FP_EMAIL, email.trim());
		session.setAttribute(SK_FP_OTP, otp);
		session.setAttribute(SK_FP_EXPIRE, expireAt);
		session.setAttribute(SK_FP_ATTEMPTS, 0);
		session.setAttribute(SK_FP_LASTSEND, now);
		session.removeAttribute(SK_FP_VERIFIED);

		try {
			// EmailUtil của bạn ở flow đăng ký có chữ ký (email, username, otp, minutes)
			var user = userOpt.get();
			emailUtil.sendForgotPassword(email.trim(), user.getUsername(), otp, FP_OTP_TTL_MIN, null);
			ra.addFlashAttribute("success", "Đã gửi mã OTP tới email. Vui lòng kiểm tra hộp thư.");
		} catch (Exception ex) {
			ra.addFlashAttribute("error", "Không thể gửi OTP lúc này. Vui lòng thử lại.");
			return "redirect:/auth/forgot";
		}

		return "redirect:/auth/forgot/verify";
	}

	@GetMapping("/forgot/verify")
	public String forgotVerifyForm(HttpServletRequest req, Model model, RedirectAttributes ra) {
		HttpSession s = req.getSession(false);
		if (s == null || s.getAttribute(SK_FP_EMAIL) == null) {
			ra.addFlashAttribute("error", "Vui lòng nhập email để nhận OTP trước.");
			return "redirect:/auth/forgot";
		}
		Integer attempts = (Integer) s.getAttribute(SK_FP_ATTEMPTS);
		int attemptsLeft = FP_MAX_ATTEMPTS - (attempts == null ? 0 : attempts);
		model.addAttribute("email", s.getAttribute(SK_FP_EMAIL));
		model.addAttribute("attemptsLeft", attemptsLeft);
		return "auth/verify-reset-otp";
	}

	@PostMapping("/forgot/verify")
	public String forgotVerifyHandle(@RequestParam("otp") String otp, HttpServletRequest req, RedirectAttributes ra) {
		HttpSession s = req.getSession(false);
		if (s == null) {
			ra.addFlashAttribute("error", "Phiên khôi phục không hợp lệ. Vui lòng làm lại.");
			return "redirect:/auth/forgot";
		}

		String saved = (String) s.getAttribute(SK_FP_OTP);
		Long expire = (Long) s.getAttribute(SK_FP_EXPIRE);
		Integer attempts = (Integer) s.getAttribute(SK_FP_ATTEMPTS);
		if (attempts == null)
			attempts = 0;

		if (saved == null || expire == null) {
			ra.addFlashAttribute("error", "OTP không tồn tại hoặc đã hết hạn. Vui lòng gửi lại.");
			clearForgotSession(s);
			return "redirect:/auth/forgot";
		}
		if (System.currentTimeMillis() > expire) {
			ra.addFlashAttribute("error", "OTP đã hết hạn. Vui lòng gửi lại.");
			clearForgotSession(s);
			return "redirect:/auth/forgot";
		}
		if (attempts >= FP_MAX_ATTEMPTS) {
			ra.addFlashAttribute("error", "Bạn đã nhập sai quá số lần cho phép. Vui lòng gửi lại OTP.");
			clearForgotSession(s);
			return "redirect:/auth/forgot";
		}
		if (!StringUtils.hasText(otp) || !saved.equals(otp.trim())) {
			s.setAttribute(SK_FP_ATTEMPTS, attempts + 1);
			ra.addFlashAttribute("error", "OTP không đúng. Vui lòng thử lại.");
			return "redirect:/auth/forgot/verify";
		}

		s.setAttribute(SK_FP_VERIFIED, true);
		ra.addFlashAttribute("success", "Xác minh OTP thành công. Vui lòng đặt mật khẩu mới.");
		return "redirect:/auth/reset";
	}

	@GetMapping("/reset")
	public String resetForm(HttpServletRequest req, RedirectAttributes ra) {
		HttpSession s = req.getSession(false);
		if (s == null || !Boolean.TRUE.equals(s.getAttribute(SK_FP_VERIFIED))) {
			ra.addFlashAttribute("error", "Phiên khôi phục không hợp lệ. Vui lòng thực hiện lại.");
			return "redirect:/auth/forgot";
		}
		return "auth/reset-password";
	}

	@PostMapping("/reset")
	public String handleReset(@RequestParam("password") String newPassword,
			@RequestParam("confirm") String confirmPassword, HttpSession session, HttpServletRequest request,
			HttpServletResponse response, RedirectAttributes ra) {

		// 1️ Kiểm tra phiên OTP hợp lệ
		String email = (String) session.getAttribute("FP_EMAIL");
		Boolean verified = (Boolean) session.getAttribute("FP_VERIFIED");

		if (email == null || !Boolean.TRUE.equals(verified)) {
			ra.addFlashAttribute("error", "Phiên khôi phục không hợp lệ hoặc đã hết hạn.");
			return "redirect:/auth/forgot";
		}

		// 2️ Kiểm tra dữ liệu nhập
		if (!StringUtils.hasText(newPassword) || !StringUtils.hasText(confirmPassword)) {
			ra.addFlashAttribute("error", "Vui lòng nhập đầy đủ thông tin mật khẩu.");
			return "redirect:/auth/reset";
		}
		if (!newPassword.equals(confirmPassword)) {
			ra.addFlashAttribute("error", "Xác nhận mật khẩu không khớp.");
			return "redirect:/auth/reset";
		}
		if (newPassword.length() < 8) {
			ra.addFlashAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
			return "redirect:/auth/reset";
		}

		// 3️ Tìm user theo email
		var userOpt = userService.findByEmail(email);
		if (userOpt.isEmpty()) {
			ra.addFlashAttribute("error", "Tài khoản không tồn tại hoặc đã bị khóa.");
			clearForgotSession(session);
			return "redirect:/auth/forgot";
		}

		var user = userOpt.get();

		// 4️ Cập nhật mật khẩu mới
		user.setPassword(com.womtech.util.PasswordUtil.encode(newPassword));
		userService.save(user);

		// 5️ Thu hồi cookie / token đăng nhập (nếu có)
		clearAuthCookies(request, response);
		session.invalidate();

		// 6️ Hoàn tất
		ra.addFlashAttribute("success", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.");
		return "redirect:/auth/login";
	}

	private void clearAuthCookies(HttpServletRequest request, HttpServletResponse response) {
		CookieUtil.delete(request, response, CK_AT);
		CookieUtil.delete(request, response, CK_RT);
		CookieUtil.delete(request, response, CK_REMEMBER);

		// Xóa JSESSIONID (host-only, không domain cho local)
		Cookie js = new Cookie("JSESSIONID", "");
		js.setPath("/");
		js.setHttpOnly(true);
		js.setMaxAge(0);
		response.addCookie(js);

		// Nếu muốn chắc cú SameSite cho các trình duyệt cũ:
		response.addHeader("Set-Cookie",
				"JSESSIONID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
	}

	private static String generateOtp() {
		return String.format("%06d", new Random().nextInt(1_000_000));
	}

	private static String maskEmail(String email) {
		if (email == null || !email.contains("@"))
			return "email của bạn";
		String[] parts = email.split("@", 2);
		String local = parts[0];
		if (local.length() <= 2)
			return local.charAt(0) + "***@" + parts[1];
		return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + parts[1];
	}

	private static void clearOtpSession(HttpSession session) {
		session.removeAttribute(SK_REG_PENDING);
		session.removeAttribute(SK_REG_EMAIL_MASK);
		session.removeAttribute(SK_OTP_CODE);
		session.removeAttribute(SK_OTP_EXPIRE);
		session.removeAttribute(SK_OTP_LAST_SENT);
	}

	private String resolveRedirectByRoles(List<String> roles) {
		if (roles == null)
			return "/";
		Set<String> rs = roles.stream().filter(Objects::nonNull).map(String::toUpperCase).collect(Collectors.toSet());
		if (rs.contains("ADMIN"))
			return "/admin/dashboard";
		if (rs.contains("VENDOR"))
			return "/vendor/dashboard";
		if (rs.contains("SHIPPER"))
			return "/shipper/dashboard";
		return "/";
	}

	private void clearForgotSession(HttpSession s) {
		s.removeAttribute(SK_FP_EMAIL);
		s.removeAttribute(SK_FP_OTP);
		s.removeAttribute(SK_FP_EXPIRE);
		s.removeAttribute(SK_FP_ATTEMPTS);
		s.removeAttribute(SK_FP_LASTSEND);
		s.removeAttribute(SK_FP_VERIFIED);
	}
}