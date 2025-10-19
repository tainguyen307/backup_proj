package com.womtech.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.womtech.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
	Page<ChatMessage> findByChat_ChatIDOrderBySendTimeDesc(String chatID, Pageable pageable);

    ChatMessage findFirstByChat_ChatIDOrderBySendTimeDesc(String chatID);
}