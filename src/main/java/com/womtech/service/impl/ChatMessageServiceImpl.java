package com.womtech.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import com.womtech.entity.ChatMessage;
import com.womtech.service.ChatMessageService;

@Service
public class ChatMessageServiceImpl extends BaseServiceImpl<ChatMessage, String> implements ChatMessageService {
	public ChatMessageServiceImpl(JpaRepository<ChatMessage, String> repo) {
		super(repo);
	}
}