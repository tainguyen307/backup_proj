package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "postID", length = 36)
	private String postID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "userID", foreignKey = @ForeignKey(name = "fk_posts_users"))
	private User user;

	@Column(nullable = false, length = 200)
	private String title;
	@Column(nullable = false, length = 50)
	private String type;
	@Lob
	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
	private String content;
	@Column(length = 255)
	private String thumbnail;

	@CreationTimestamp
	@Column(name = "create_at", updatable = false)
	private LocalDateTime createAt;
	@UpdateTimestamp
	@Column(name = "update_at")
	private LocalDateTime updateAt;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;
}