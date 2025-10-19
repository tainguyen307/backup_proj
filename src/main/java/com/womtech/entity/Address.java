package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "addressID", length = 36)
	private String addressID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "userID", foreignKey = @ForeignKey(name = "fk_addresses_users"))
	private User user;

	@Column(nullable = false, length = 150)
	private String fullname;
	@Column(nullable = false, length = 50)
	private String phone;
	@Column(nullable = false, length = 255)
	private String street;
	@Column(nullable = false, length = 150)
	private String ward;
	@Column(nullable = false, length = 150)
	private String district;
	@Column(nullable = false, length = 150)
	private String city;

	@Builder.Default
	@Column(name = "is_default", nullable = false)
	private boolean isDefault = false;

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
	@OneToMany(mappedBy = "address")
	private List<Order> orders = new ArrayList<>();
}