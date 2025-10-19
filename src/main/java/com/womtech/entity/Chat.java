package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "chats", indexes = @Index(name = "idx_chats_user", columnList = "userID"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "chatID", length = 36)
	private String chatID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "userID", foreignKey = @ForeignKey(name = "fk_chats_user"))
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "supportID", foreignKey = @ForeignKey(name = "fk_chats_support"))
	private User support;

	@CreationTimestamp
	@Column(name = "create_at", updatable = false)
	private LocalDateTime createAt;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;

	@Builder.Default
	@OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChatMessage> messages = new ArrayList<>();
}