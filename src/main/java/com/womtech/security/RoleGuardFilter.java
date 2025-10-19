package com.womtech.security;

import com.womtech.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@Order(10) // chạy sau JwtAuthFilter (thường Jwt đặt trước trong SecurityConfig)
public class RoleGuardFilter extends OncePerRequestFilter {

	private static final String CK_AT = "AT";

	private final JwtService jwtService;
	private final TokenRevokeService tokenRevokeService;

	public RoleGuardFilter(JwtService jwtService, TokenRevokeService tokenRevokeService) {
		this.jwtService = jwtService;
		this.tokenRevokeService = tokenRevokeService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String path = request.getRequestURI();

		// ✅ Bỏ qua các path công khai + các path có handler riêng cho lỗi
		if (isPublic(path)) {
			chain.doFilter(request, response);
			return;
		}

		// ✅ Xác định role yêu cầu — CHỈ áp cho /vendor/** (có dấu '/'), tránh ăn vào
		// /vendor-register
		String requiredRole = requiredRoleFor(path);
		if (requiredRole == null) {
			chain.doFilter(request, response);
			return;
		}

		// Lấy roles từ JWT (ưu tiên) hoặc session fallback
		Set<String> roles = extractRoles(request);

		if (roles.contains(requiredRole)) {
			chain.doFilter(request, response);
		} else {
			// Nếu chưa đăng nhập → về login; nếu đã đăng nhập nhưng thiếu quyền → access
			// denied
			if (roles.isEmpty()) {
				response.sendRedirect("/auth/login");
			} else {
				// Đừng redirect /error/403 (không có controller); dùng trang access-denied bạn
				// đã map
				response.sendRedirect("/auth/access-denied");
			}
		}
	}

	private boolean isPublic(String path) {
		// Public + cho phép explicitly các route sau
		return path.equals("/") || path.startsWith("/auth") || path.startsWith("/assets") || path.startsWith("/css")
				|| path.startsWith("/js") || path.startsWith("/images") || path.startsWith("/webjars")
				|| path.startsWith("/products") || path.startsWith("/product") || path.startsWith("/category")
				|| path.equals("/error") || path.startsWith("/error/") || path.equals("/auth/access-denied")
				// ✅ Quan trọng: bỏ qua flow đăng ký shop mới
				|| path.equals("/vendor-register") || path.startsWith("/vendor-register/");
	}

	private String requiredRoleFor(String path) {
		if (path.startsWith("/admin/") || path.equals("/admin"))
			return "ADMIN";
		if (path.startsWith("/vendor/") || path.equals("/vendor"))
			return "VENDOR"; // chỉ /vendor/... mới cần VENDOR
		if (path.startsWith("/shipper/") || path.equals("/shipper"))
			return "SHIPPER";
		return null; // không yêu cầu role cụ thể
	}

	@SuppressWarnings("unchecked")
	private Set<String> extractRoles(HttpServletRequest request) {
		// 1) JWT trong cookie AT
		Cookie at = CookieUtil.get(request, CK_AT);
		if (at != null && jwtService.isValidAccess(at.getValue()) && !tokenRevokeService.isRevoked(at.getValue())) {
			List<String> r = jwtService.getRoles(at.getValue());
			if (r != null)
				return toUpperSet(r);
		}

		// 2) Fallback session
		HttpSession session = request.getSession(false);
		if (session != null) {
			Object obj = session.getAttribute("CURRENT_ROLES");
			if (obj instanceof List<?>)
				return toUpperSet((List<String>) obj);
		}

		return Collections.emptySet();
	}

	private Set<String> toUpperSet(List<String> roles) {
		Set<String> rs = new HashSet<>();
		for (String s : roles) {
			if (s != null)
				rs.add(s.toUpperCase(Locale.ROOT));
		}
		return rs;
	}
}
