package com.womtech.api;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.womtech.entity.Chat;
import com.womtech.entity.User;
import com.womtech.repository.ChatRepository;
import com.womtech.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

	private final ChatRepository chatRepository;
	private final UserRepository userRepository;
	
	/**
	 * USER bấm từ trang sản phẩm: /api/chat/start?vendorId=... - userId lấy từ
	 * Principal (token/cookie)
	 */
	@PostMapping("/start")
	public ResponseEntity<?> startChat(@RequestParam String vendorId, Principal principal) {
		String userId = principal != null ? principal.getName() : null;
		if (userId == null || userId.isBlank()) {
			return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
		}

		
		Chat chat = chatRepository.findByUser_UserIDAndSupport_UserID(userId, vendorId).orElseGet(() -> {
			User user = userRepository.findById(userId).orElseThrow();
			User support = userRepository.findById(vendorId).orElseThrow();
			Chat c = new Chat();
			c.setUser(user);
			c.setSupport(support);
			return chatRepository.save(c);
		});

		return ResponseEntity.ok(Map.of("chatId", chat.getChatID()));
	}
}
