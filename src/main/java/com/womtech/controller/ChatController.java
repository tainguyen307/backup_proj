package com.womtech.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.entity.Chat;
import com.womtech.entity.ChatMessage;
import com.womtech.entity.User;
import com.womtech.repository.ChatMessageRepository;
import com.womtech.repository.ChatRepository;
import com.womtech.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Controller
@RequiredArgsConstructor
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRepository chatRepository;
	private final UserRepository userRepository;
	private final ChatMessageRepository chatMessageRepository;

	/**
	 * Client send: /app/chat/send/{chatId} Server push: /topic/chat/{chatId}
	 * Payload: { "message": "..." }
	 */
	@MessageMapping("/chat/send/{chatId}")
	@Transactional
	public void sendToChat(@DestinationVariable String chatId, @Payload SendMessageReq req, Principal principal) {

		// 1) Xác thực
		if (principal == null || principal.getName() == null || principal.getName().isBlank())
			return;
		final String currentUserId = principal.getName();

		// 2) Tải chat & kiểm tra thành viên
		Chat chat = chatRepository.findById(chatId).orElse(null);
		if (chat == null)
			return;
		if (!isParticipant(currentUserId, chat))
			return; // chặn gửi trái phép

		// 3) Lấy sender
		User sender = userRepository.findById(currentUserId).orElse(null);
		if (sender == null)
			return;

		// 4) Lưu message
		ChatMessage saved = chatMessageRepository.save(
				ChatMessage.builder().chat(chat).sender(sender).message(req != null ? req.getMessage() : null).build());

		// 5) Broadcast payload gọn nhẹ
		ChatMessagePayload payload = new ChatMessagePayload(chat.getChatID(), sender.getUserID(), safeName(sender),
				saved.getMessage(), saved.getSendTime() != null ? saved.getSendTime().toString() : null);

		messagingTemplate.convertAndSend("/topic/chat/" + chatId, payload);
	}

	private boolean isParticipant(String userId, Chat chat) {
		String u1 = chat.getUser() != null ? chat.getUser().getUserID() : null;
		String u2 = chat.getSupport() != null ? chat.getSupport().getUserID() : null;
		return userId != null && (userId.equals(u1) || userId.equals(u2));
	}

	private String safeName(User u) {
		try {
			if (u.getUsername() != null && !u.getUsername().isBlank())
				return u.getUsername();
		} catch (Exception ignored) {
		}
		return "User";
	}

	// === DTOs ===
	@Value
	public static class SendMessageReq {
		String message;
	}

	@Value
	public static class ChatMessagePayload {
		String chatId;
		String senderId;
		String senderName;
		String message;
		String sendTime;
	}
}
