package com.womtech.security;

import com.womtech.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final TokenRevokeService revokeService;

	public JwtAuthFilter(JwtService jwtService, TokenRevokeService revokeService) {
		this.jwtService = jwtService;
		this.revokeService = revokeService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws ServletException, IOException {

		if (getContext().getAuthentication() == null) {
			Cookie atCookie = CookieUtil.get(req, "AT");
			String at = (atCookie != null) ? atCookie.getValue() : null;

			if (at != null && jwtService.isValidAccess(at) && !revokeService.isRevoked(at)) {
				String userId = jwtService.getUserId(at);
				List<String> roles = jwtService.getRoles(at);
				if (roles == null)
					roles = List.of();

				Collection<SimpleGrantedAuthority> auths = new ArrayList<>();
				for (String r : roles) {
					if (r != null && !r.isBlank()) {
						auths.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()));
					}
				}

				var authentication = new UsernamePasswordAuthenticationToken(userId, null, auths);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
				getContext().setAuthentication(authentication);

			} else if (at != null) {
				// Có token nhưng invalid/revoked → xoá sạch cookie theo đúng domain/SameSite
				CookieUtil.delete(req, res, "AT");
				CookieUtil.delete(req, res, "RT");
				CookieUtil.delete(req, res, "WOM_REMEMBER");
			}
		}

		chain.doFilter(req, res);
	}
}
