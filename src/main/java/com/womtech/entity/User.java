package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users", indexes = { @Index(name = "uk_users_username", columnList = "username", unique = true),
		@Index(name = "uk_users_email", columnList = "email", unique = true) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "userID", length = 36)
	private String userID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "roleID", foreignKey = @ForeignKey(name = "fk_users_roles"))
	private Role role;

	@Column(nullable = false, unique = true, length = 150)
	private String username;
	@Column(nullable = false, length = 255)
	private String password;
	@Column(nullable = false, unique = true, length = 255)
	private String email;
    @Column(length = 500)
    private String avatar;

	@CreationTimestamp
	@Column(name = "create_at", updatable = false)
	private LocalDateTime createAt;
	@UpdateTimestamp
	@Column(name = "update_at")
	private LocalDateTime updateAt;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;

	@Builder.Default
	@OneToMany(mappedBy = "user")
	private List<Post> posts = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "user")
	private List<Review> reviews = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "user")
	private List<Address> addresses = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "user")
	private List<Order> orders = new ArrayList<>();

	@OneToOne(mappedBy = "user")
	private Cart cart;

	@Builder.Default
	@OneToMany(mappedBy = "user")
	private List<Chat> chats = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "support")
	private List<Chat> supportedChats = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "sender")
	private List<ChatMessage> sentMessages = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "owner")
	private List<Voucher> vouchers = new ArrayList<>();
}