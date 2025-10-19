package com.womtech.service;

import com.womtech.dto.request.chat.CreateChatRequest;
import com.womtech.dto.request.chat.SendMessageRequest;
import com.womtech.dto.response.chat.ChatMessageResponse;
import com.womtech.dto.response.chat.ChatSummaryResponse;
import com.womtech.entity.Chat;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ChatService extends BaseService<Chat, String> {
   
    ChatSummaryResponse createOrGetChat(String userID, CreateChatRequest req);

    List<ChatSummaryResponse> listChatsOfUser(String userID);

    List<ChatSummaryResponse> listChatsOfSupport(String supportID);

    Page<ChatMessageResponse> getMessages(String chatID, int page, int size);

    ChatMessageResponse sendMessage(String chatID, String senderID, SendMessageRequest req);
}
