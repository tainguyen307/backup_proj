package com.womtech.dto.response.chat;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
	private String chatMessageID;
    private String chatID;
    private String senderID;
    private String senderName;  
    private String message;
    private LocalDateTime sendTime;
}
