package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "vouchers", indexes = @Index(name = "idx_vouchers_code", columnList = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "voucherID", length = 36)
	private String voucherID;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userID", foreignKey = @ForeignKey(name = "fk_vouchers_users"))
	private User owner;

	@Column(nullable = false, length = 50)
	private String code;

	@Builder.Default
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal discount = BigDecimal.ZERO;

	@Column(precision = 12, scale = 2)
	private BigDecimal min_price;
	private LocalDateTime expire_date;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;

	@Builder.Default
	@OneToMany(mappedBy = "voucher")
	private List<Order> orders = new ArrayList<>();
}