package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = @Index(name = "idx_reviews_product", columnList = "productID"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "reviewID", length = 36)
	private String reviewID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "productID", foreignKey = @ForeignKey(name = "fk_reviews_products"))
	private Product product;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "userID", foreignKey = @ForeignKey(name = "fk_reviews_users"))
	private User user;

	@Column(nullable = false)
	private Integer rating;
	@Lob
	private String comment;

	@CreationTimestamp
	@Column(name = "create_at", updatable = false)
	private LocalDateTime createAt;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;
}