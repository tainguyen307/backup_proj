package com.womtech.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	private final JwtService jwtService;
	private final TokenRevokeService revokeService;

	public SecurityConfig(JwtService jwtService, TokenRevokeService revokeService) {
		this.jwtService = jwtService;
		this.revokeService = revokeService;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.securityContext(sc -> sc.requireExplicitSave(false))

				.authorizeHttpRequests(auth -> auth
						// ✅ ĐẶT TRÊN CÙNG: trang đăng ký shop (chỉ cần đăng nhập)
						.requestMatchers(HttpMethod.GET, "/vendor-register", "/vendor-register/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/vendor-register", "/vendor-register/**").authenticated()
						
						.requestMatchers("/order/vnpay-return").permitAll()
						
						// Static
						.requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/static/**", "/webjars/**",
								"/favicon.ico")
						.permitAll().requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

						// Public pages
						.requestMatchers("/", "/auth/**", "/products/**", "/about", "/contact").permitAll()

						// WebSocket / chat
						.requestMatchers("/user/chat", "/user/chat-ws/**").authenticated()
						.requestMatchers("/vendor/chat", "/vendor/chat-ws/**").hasRole("VENDOR")

						// REST cho chat
						.requestMatchers("/api/chat/**", "/api/chats/**").authenticated()

						// Khu vực role-based
						.requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/vendor/**").hasRole("VENDOR") // chỉ
																														// áp
																														// cho
																														// /vendor/...,
																														// không
																														// ảnh
																														// hưởng
																														// /vendor-register
						.requestMatchers("/shipper/**").hasRole("SHIPPER")

						// Error
						.requestMatchers("/error", "/error/**").permitAll()

						// Default
						.anyRequest().permitAll())

				.exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> res.sendRedirect("/auth/login"))
						.accessDeniedHandler((req, res, e) -> res.sendRedirect("/auth/access-denied")))

				// JWT filter
				.addFilterBefore(new JwtAuthFilter(jwtService, revokeService),
						UsernamePasswordAuthenticationFilter.class)

				.formLogin(form -> form.disable()).logout(logout -> logout.disable()).httpBasic(h -> h.disable());

		return http.build();
	}
}
