package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = @Index(name = "idx_chat_messages_chat", columnList = "chatID"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "chat_messageID", length = 36)
	private String chatMessageID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "chatID", foreignKey = @ForeignKey(name = "fk_chatmsg_chat"))
	private Chat chat;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "senderID", foreignKey = @ForeignKey(name = "fk_chatmsg_sender"))
	private User sender;

	@Lob
	@Column(nullable = false)
	private String message;

	@CreationTimestamp
	@Column(name = "send_time", updatable = false)
	private LocalDateTime sendTime;
}
