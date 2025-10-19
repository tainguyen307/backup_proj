package com.womtech.api;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.womtech.dto.response.chat.ChatMessageResponse;
import com.womtech.dto.response.chat.ChatSummaryResponse;
import com.womtech.entity.Chat;
import com.womtech.entity.ChatMessage;
import com.womtech.entity.User;
import com.womtech.repository.ChatMessageRepository;
import com.womtech.repository.ChatRepository;
import com.womtech.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatQueryController {

	private final ChatRepository chatRepo;
	private final ChatMessageRepository msgRepo;
	private final UserRepository userRepo;

	/**
	 * Sidebar: trả về tất cả cuộc chat mà current user tham gia (buyer hoặc vendor)
	 */
	@GetMapping("/me")
	public List<ChatSummaryResponse> myChats(Principal principal) {
	    String uid = requireUid(principal);

	    // Lấy mọi chat có user = uid hoặc support = uid
	    var chats = chatRepo.findByUser_UserIDOrSupport_UserID(uid, uid);

	    // map ra DTO ChatSummaryResponse
	    return chats.stream().map(c -> {
	        ChatMessage last = msgRepo.findFirstByChat_ChatIDOrderBySendTimeDesc(c.getChatID());
	        return ChatSummaryResponse.builder()
	                .chatID(c.getChatID())
	                .userID(c.getUser() != null ? c.getUser().getUserID() : null)
	                .supportID(c.getSupport() != null ? c.getSupport().getUserID() : null)
	                .supportName(c.getSupport() != null ? c.getSupport().getUsername() : null)
	                .status(c.getStatus())
	                .createAt(c.getCreateAt())
	                .lastMessage(last != null ? last.getMessage() : null)
	                .lastTime(last != null ? last.getSendTime() : null)
	                .build();
	    }).toList();
	}

	/** Lịch sử tin nhắn theo chatId (DESC). JS sẽ đảo chiều để hiển thị. */
	@GetMapping("/{chatId}/messages")
	public Page<ChatMessageResponse> listMessages(@PathVariable String chatId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size,
			Principal principal) {
		ensureParticipant(chatId, principal);

		var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sendTime"));
		var pg = msgRepo.findByChat_ChatIDOrderBySendTimeDesc(chatId, pageable);

		return pg.map(m -> ChatMessageResponse.builder().chatMessageID(m.getChatMessageID())
				.chatID(m.getChat().getChatID()).senderID(m.getSender() != null ? m.getSender().getUserID() : null)
				.senderName(m.getSender() != null ? m.getSender().getUsername() : null).message(m.getMessage())
				.sendTime(m.getSendTime()).build());
	}

	/**
	 * Nút “+ Tạo chat” (tùy chọn). - Truyền supportID qua query (?supportID=...)
	 * HOẶC body JSON { "supportID": "..." }. - Nếu đã tồn tại chat giữa 2 người thì
	 * trả về chat cũ.
	 */
	@PostMapping
	public ChatSummaryResponse createChat(@RequestParam(required = false) String supportID,
			@RequestBody(required = false) CreateChatReq body, Principal principal) {
		String uid = requireUid(principal);
		String vendorId = supportID != null && !supportID.isBlank() ? supportID
				: (body != null ? body.supportID : null);

		if (vendorId == null || vendorId.isBlank()) {
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Thiếu supportID (vendor)");
		}

		Chat chat = chatRepo.findByUser_UserIDAndSupport_UserID(uid, vendorId).orElseGet(() -> {
			User u = userRepo.findById(uid).orElseThrow();
			User v = userRepo.findById(vendorId).orElseThrow();
			Chat c = new Chat();
			c.setUser(u);
			c.setSupport(v);
			return chatRepo.save(c);
		});

		ChatMessage last = msgRepo.findFirstByChat_ChatIDOrderBySendTimeDesc(chat.getChatID());

		return ChatSummaryResponse.builder().chatID(chat.getChatID())
				.userID(chat.getUser() != null ? chat.getUser().getUserID() : null)
				.supportID(chat.getSupport() != null ? chat.getSupport().getUserID() : null)
				.supportName(chat.getSupport() != null ? chat.getSupport().getUsername() : null)
				.status(chat.getStatus()).createAt(chat.getCreateAt())
				.lastMessage(last != null ? last.getMessage() : null).lastTime(last != null ? last.getSendTime() : null)
				.build();
	}

	// ===== Helpers =====
	private String requireUid(Principal p) {
		if (p == null || p.getName() == null || p.getName().isBlank()) {
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		return p.getName();
	}

	private void ensureParticipant(String chatId, Principal p) {
		String uid = requireUid(p);
		Chat c = chatRepo.findById(chatId)
				.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
		String u1 = c.getUser() != null ? c.getUser().getUserID() : null;
		String u2 = c.getSupport() != null ? c.getSupport().getUserID() : null;
		if (!uid.equals(u1) && !uid.equals(u2)) {
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
		}
	}

	// Body type tối giản để nhận { "supportID": "..." }
	private record CreateChatReq(String supportID) {
	}
}