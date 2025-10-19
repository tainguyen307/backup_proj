package com.womtech.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class CookieUtil {

	private CookieUtil() {
	}

	// --- Helpers ---
	private static boolean isLocalHost(String host) {
		if (host == null)
			return true;
		String h = host.toLowerCase();
		return "localhost".equals(h) || "127.0.0.1".equals(h) || h.endsWith(".local");
	}

	private static String normalizeDomain(String domain) {
		if (domain == null)
			return null;
		return domain.startsWith(".") ? domain.substring(1) : domain; // bỏ dấu chấm đầu
	}

	private static String resolveCookieDomain(HttpServletRequest req) {
		String host = req != null ? req.getServerName() : null;
		if (host == null)
			return null;
		String d = normalizeDomain(host);
		return isLocalHost(d) ? null : d; // local => KHÔNG set domain
	}

	// --- Public APIs ---
	public static void add(HttpServletResponse response, String name, String value, int maxAgeSec, boolean httpOnly,
			boolean secure, String sameSite) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeSec);
		cookie.setHttpOnly(httpOnly);
		cookie.setSecure(secure);
		// KHÔNG set domain ở đây: dùng host-only cookie cho local là chuẩn nhất
		response.addCookie(cookie);

		// Servlet Cookie chưa có SameSite -> thêm header phụ (không kèm Domain cho
		// local)
		if (sameSite != null && !sameSite.isBlank()) {
			String headerValue = buildHeaderValue(name, value, maxAgeSec, httpOnly, secure, sameSite, null);
			response.addHeader("Set-Cookie", headerValue);
		}
	}

	public static Cookie get(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie c : cookies) {
			if (name.equals(c.getName()))
				return c;
		}
		return null;
	}

	// Xóa cookie theo host hiện tại (local: không set domain; prod: set domain hợp
	// lệ)
	public static void delete(HttpServletRequest request, HttpServletResponse response, String name) {
		String domain = resolveCookieDomain(request);

		// 1) Bằng API Cookie (host-only nếu domain null)
		Cookie c = new Cookie(name, "");
		c.setPath("/");
		c.setMaxAge(0);
		c.setHttpOnly(true);
		// Chỉ set domain nếu hợp lệ (không phải localhost)
		if (domain != null)
			c.setDomain(domain);
		response.addCookie(c);

		// 2) Bằng header Set-Cookie (thêm Expires cho compatibility)
		String header = buildHeaderValue(name, "", 0, true, false, "Strict", domain)
				+ "; Expires=Thu, 01 Jan 1970 00:00:00 GMT";
		response.addHeader("Set-Cookie", header);
	}

	// --- Internal ---
	private static String buildHeaderValue(String name, String value, int maxAgeSec, boolean httpOnly, boolean secure,
			String sameSite, String domain) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append('=').append(value == null ? "" : value);
		sb.append("; Path=/");
		sb.append("; Max-Age=").append(maxAgeSec);
		if (domain != null && !domain.isBlank()) {
			sb.append("; Domain=").append(normalizeDomain(domain));
		}
		if (httpOnly)
			sb.append("; HttpOnly");
		if (secure)
			sb.append("; Secure");
		if (sameSite != null && !sameSite.isBlank()) {
			sb.append("; SameSite=").append(sameSite);
		}
		return sb.toString();
	}
}
