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
public class ChatSummaryResponse {
	private String chatID;
	private String userID;
	private String supportID;
	private String supportName; 
	private Integer status;
	private LocalDateTime createAt;
	private String lastMessage;
	private LocalDateTime lastTime;
}
