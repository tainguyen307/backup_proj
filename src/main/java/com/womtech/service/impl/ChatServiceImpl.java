package com.womtech.service.impl;

import com.womtech.dto.request.chat.CreateChatRequest;
import com.womtech.dto.request.chat.SendMessageRequest;
import com.womtech.dto.response.chat.ChatMessageResponse;
import com.womtech.dto.response.chat.ChatSummaryResponse;
import com.womtech.entity.Chat;
import com.womtech.entity.ChatMessage;
import com.womtech.entity.User;
import com.womtech.repository.ChatMessageRepository;
import com.womtech.repository.ChatRepository;
import com.womtech.repository.UserRepository;
import com.womtech.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@Transactional
public class ChatServiceImpl extends BaseServiceImpl<Chat, String> implements ChatService {

    private final ChatRepository chatRepo;
    private final ChatMessageRepository msgRepo;
    private final UserRepository userRepo;

    // ✅ phải có constructor thủ công để truyền chatRepo vào super()
    public ChatServiceImpl(ChatRepository chatRepo,
                           ChatMessageRepository msgRepo,
                           UserRepository userRepo) {
        super(chatRepo);
        this.chatRepo = chatRepo;
        this.msgRepo = msgRepo;
        this.userRepo = userRepo;
    }

    // =================================================================
    // 1️⃣ Tạo hoặc lấy phòng chat giữa user và support
    // =================================================================
    @Override
    public ChatSummaryResponse createOrGetChat(String userID, CreateChatRequest req) {
        User user = userRepo.findById(userID)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userID));

        String supportID = (req != null) ? req.getSupportID() : null;
        User support = null;

        // Nếu có supportID thì kiểm tra xem cặp này đã có chat chưa
        if (supportID != null && !supportID.isBlank()) {
            support = userRepo.findById(supportID)
                    .orElseThrow(() -> new IllegalArgumentException("Support not found: " + supportID));
            var existed = chatRepo.findByUser_UserIDAndSupport_UserID(userID, supportID);
            if (existed.isPresent()) {
                log.debug("Existing chat found between user {} and support {}", userID, supportID);
                return toSummaryWithLast(existed.get());
            }
        }

        // Nếu chưa có thì tạo mới
        Chat chat = Chat.builder()
                .user(user)
                .support(support)
                .status(1)
                .build();

        chat = chatRepo.save(chat);
        log.info("Created new chat {} for user {}", chat.getChatID(), userID);

        return toSummary(chat);
    }

    // =================================================================
    // 2️⃣ Danh sách chat của user
    // =================================================================
    @Override
    @Transactional(readOnly = true)
    public List<ChatSummaryResponse> listChatsOfUser(String userID) {
        var list = chatRepo.findByUser_UserIDOrderByCreateAtDesc(userID);
        return list.stream()
                .map(this::toSummaryWithLast)
                .sorted(byLastTimeDesc())
                .toList();
    }

    // =================================================================
    // 3️⃣ Danh sách chat của support
    // =================================================================
    @Override
    @Transactional(readOnly = true)
    public List<ChatSummaryResponse> listChatsOfSupport(String supportID) {
        var list = chatRepo.findBySupport_UserIDOrderByCreateAtDesc(supportID);
        return list.stream()
                .map(this::toSummaryWithLast)
                .sorted(byLastTimeDesc())
                .toList();
    }

    // =================================================================
    // 4️⃣ Lấy lịch sử tin nhắn trong 1 chat
    // =================================================================
    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(String chatID, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return msgRepo.findByChat_ChatIDOrderBySendTimeDesc(chatID, pageable)
                .map(this::toMessageResponse);
    }

    // =================================================================
    // 5️⃣ Gửi tin nhắn trong 1 chat
    // =================================================================
    @Override
    public ChatMessageResponse sendMessage(String chatID, String senderID, SendMessageRequest req) {
        if (req == null || req.getMessage() == null || req.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message must not be blank.");
        }

        Chat chat = chatRepo.findById(chatID)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chatID));

        // Kiểm tra thành viên
        boolean isMember = (chat.getUser() != null && senderID.equals(chat.getUser().getUserID()))
                        || (chat.getSupport() != null && senderID.equals(chat.getSupport().getUserID()));

        // Nếu chưa gán support thì cho phép user gửi
        if (!isMember) {
            if (!(chat.getSupport() == null && chat.getUser() != null
                    && senderID.equals(chat.getUser().getUserID()))) {
                throw new SecurityException("Sender is not a member of this chat.");
            }
        }

        User sender = userRepo.findById(senderID)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderID));

        ChatMessage entity = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .message(req.getMessage())
                .build();

        entity = msgRepo.save(entity);
        log.info("New message in chat {} by {}", chatID, senderID);

        return toMessageResponse(entity);
    }

    // =================================================================
    // 🔄 Helper methods: mapping entity -> response DTO
    // =================================================================
    private ChatSummaryResponse toSummary(Chat c) {
        return ChatSummaryResponse.builder()
                .chatID(c.getChatID())
                .userID(c.getUser() != null ? c.getUser().getUserID() : null)
                .supportID(c.getSupport() != null ? c.getSupport().getUserID() : null)
                .supportName(c.getSupport() != null ? displayName(c.getSupport()) : null)
                .status(c.getStatus())
                .createAt(c.getCreateAt())
                .build();
    }

    private ChatSummaryResponse toSummaryWithLast(Chat c) {
        var dto = toSummary(c);
        var last = msgRepo.findFirstByChat_ChatIDOrderBySendTimeDesc(c.getChatID());
        if (last != null) {
            dto.setLastMessage(last.getMessage());
            dto.setLastTime(last.getSendTime());
        } else {
            dto.setLastMessage(null);
            dto.setLastTime(c.getCreateAt());
        }
        return dto;
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .chatMessageID(m.getChatMessageID())
                .chatID(m.getChat().getChatID())
                .senderID(m.getSender().getUserID())
                .senderName(displayName(m.getSender()))
                .message(m.getMessage())
                .sendTime(m.getSendTime())
                .build();
    }

    private Comparator<ChatSummaryResponse> byLastTimeDesc() {
        return Comparator.comparing(ChatSummaryResponse::getLastTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    }

    private String displayName(User u) {
        try {
            if (u.getUsername() != null && !u.getUsername().isBlank())
                return u.getUsername();
        } catch (Throwable ignored) {}
        try {
            if (u.getUsername() != null && !u.getUsername().isBlank())
                return u.getUsername();
        } catch (Throwable ignored) {}
        return u.getUserID();
    }
}
