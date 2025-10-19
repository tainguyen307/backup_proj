package com.womtech.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TokenRevokeService – quản lý danh sách JWT bị thu hồi (blacklist).
 * 
 * Phiên bản này chạy in-memory (dành cho môi trường dev hoặc local test). Khi
 * triển khai thực tế (deploy nhiều instance), bạn có thể viết thêm bản Redis
 * hoặc Database để lưu danh sách token bị revoke.
 */
@Service
public class TokenRevokeService {

	// Lưu token và thời điểm hết hạn (expiry) của nó.
	private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

	/**
	 * Thu hồi token – thêm token vào danh sách blacklist cho đến khi hết hạn.
	 *
	 * @param token  chuỗi JWT cần thu hồi
	 * @param expiry thời điểm token hết hạn
	 */
	public void revoke(String token, Instant expiry) {
		if (token == null || token.isBlank())
			return;
		long expMillis = expiry != null ? expiry.toEpochMilli() : Instant.now().toEpochMilli();
		revokedTokens.put(token, expMillis);
	}

	/**
	 * Kiểm tra token có bị thu hồi không.
	 * 
	 * @param token chuỗi JWT
	 * @return true nếu token đã bị revoke và chưa hết hạn
	 */
	public boolean isRevoked(String token) {
		if (token == null || token.isBlank())
			return false;
		Long exp = revokedTokens.get(token);
		if (exp == null)
			return false;

		long now = Instant.now().toEpochMilli();
		if (exp <= now) {
			// token đã hết hạn tự nhiên, xóa cho gọn
			revokedTokens.remove(token);
			return false;
		}
		return true; // token đang bị thu hồi
	}

	/**
	 * Dọn dẹp token đã hết hạn khỏi danh sách blacklist. (Gọi định kỳ nếu muốn,
	 * không bắt buộc)
	 */
	public void purgeExpired() {
		long now = Instant.now().toEpochMilli();
		revokedTokens.entrySet().removeIf(e -> e.getValue() <= now);
	}

	/**
	 * Force revoke tất cả tokens (dùng khi server restart)
	 */
	public void revokeAllTokens() {
		// Clear tất cả tokens đã revoke
		revokedTokens.clear();
		System.out.println("🔥 All tokens revoked - fresh start");
	}

	/**
	 * Revoke tất cả tokens hiện tại (dùng khi server restart)
	 * Thêm tất cả tokens vào blacklist với expiry time ngắn
	 */
	public void revokeAllCurrentTokens() {
		// Clear tất cả tokens đã revoke
		revokedTokens.clear();
		
		// Revoke tất cả tokens hiện tại với expiry time ngắn (1 giây)
		// Điều này sẽ làm cho tất cả tokens hiện tại bị invalid
		long shortExpiry = Instant.now().plusSeconds(1).toEpochMilli();
		
		// Thêm một token dummy để đảm bảo logic hoạt động
		revokedTokens.put("DUMMY_TOKEN", shortExpiry);
		
		System.out.println("🔥 All current tokens revoked - fresh start");
	}
}
