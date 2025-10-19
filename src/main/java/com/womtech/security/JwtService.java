package com.womtech.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class JwtService {

	@Value("${app.jwt.access.secret}")
	private String baseAccessSecret;

	@Value("${app.jwt.refresh.secret}")
	private String baseRefreshSecret;
	
	// Dynamic secrets that change on server restart
	private String accessSecret;
	private String refreshSecret;

	@Value("${app.jwt.access.ttl-minutes}")
	private long accessTtlMinutes;

	@Value("${app.jwt.refresh.ttl-days}")
	private long refreshTtlDays;
	
	// Initialize dynamic secrets on startup
	@jakarta.annotation.PostConstruct
	public void initSecrets() {
		String timestamp = String.valueOf(System.currentTimeMillis());
		this.accessSecret = baseAccessSecret + "_" + timestamp;
		this.refreshSecret = baseRefreshSecret + "_" + timestamp;
		System.out.println("🔑 JWT secrets regenerated - all old tokens invalidated");
	}

	private SecretKey key(String raw) {
		// đảm bảo dài ít nhất 32 ký tự
		byte[] bytes = Decoders.BASE64.decode(Base64.getEncoder().encodeToString(raw.getBytes()));
		return Keys.hmacShaKeyFor(bytes);
	}

	// ======== ACCESS TOKEN ======== //
	public String generateAccessToken(String userId, String username, List<String> roles) {
		Instant now = Instant.now();
		return Jwts.builder().setSubject(userId).claim("username", username).claim("roles", roles)
				.setIssuedAt(Date.from(now)).setExpiration(Date.from(now.plusSeconds(accessTtlMinutes * 60)))
				.signWith(key(accessSecret)).compact();
	}

	// ======== REFRESH TOKEN ======== //
	public String generateRefreshToken(String userId) {
		Instant now = Instant.now();
		return Jwts.builder().setSubject(userId).claim("typ", "refresh").setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plusSeconds(refreshTtlDays * 24 * 3600))).signWith(key(refreshSecret))
				.compact();
	}

	// ======== VALIDATION ======== //
	public boolean isValidAccess(String token) {
		return validate(token, accessSecret);
	}

	public boolean isValidRefresh(String token) {
		return validate(token, refreshSecret);
	}

	private boolean validate(String token, String secret) {
		try {
			Jwts.parserBuilder().setSigningKey(key(secret)).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	// ======== PARSE & UTIL ======== //
	private Jws<Claims> parseAny(String token) {
		try {
			// thử parse bằng access key trước, nếu lỗi thì refresh
			try {
				return Jwts.parserBuilder().setSigningKey(key(accessSecret)).build().parseClaimsJws(token);
			} catch (JwtException e) {
				return Jwts.parserBuilder().setSigningKey(key(refreshSecret)).build().parseClaimsJws(token);
			}
		} catch (JwtException e) {
			return null;
		}
	}

	public String getUserId(String token) {
		var jws = parseAny(token);
		return (jws == null) ? null : jws.getBody().getSubject();
	}

	public List<String> getRoles(String token) {
		var jws = parseAny(token);
		if (jws == null)
			return Collections.emptyList();
		Object raw = jws.getBody().get("roles");
		if (raw instanceof List<?> list) {
			return list.stream().map(String::valueOf).toList();
		}
		return Collections.emptyList();
	}

	public Instant getExpiry(String token) {
		var jws = parseAny(token);
		return (jws == null) ? Instant.EPOCH : jws.getBody().getExpiration().toInstant();
	}

	public String getUsername(String token) {
		var jws = parseAny(token);
		return (jws == null) ? null : jws.getBody().get("username", String.class);
	}

	public void printTokenInfo(String token) {
		var jws = parseAny(token);
		if (jws == null) {
			System.out.println("Invalid token");
			return;
		}
		var body = jws.getBody();
		System.out.printf("sub: %s%nusername: %s%nroles: %s%niat: %s%nexp: %s%n", body.getSubject(),
				body.get("username"), body.get("roles"),
				ZonedDateTime.ofInstant(body.getIssuedAt().toInstant(), ZoneId.systemDefault()),
				ZonedDateTime.ofInstant(body.getExpiration().toInstant(), ZoneId.systemDefault()));
	}
}
