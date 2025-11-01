package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders", indexes = { @Index(name = "idx_orders_user", columnList = "userID")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "orderID", length = 36)
	private String orderID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "userID", foreignKey = @ForeignKey(name = "fk_orders_users"))
	private User user;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "addressID", foreignKey = @ForeignKey(name = "fk_orders_addresses"))
	private Address address;

	@Builder.Default
	@Column(name = "total_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalPrice = BigDecimal.ZERO;

	@Column(name = "payment_method", nullable = false, length = 50)
	private String paymentMethod;

	@Builder.Default
	@Column(name = "payment_status", nullable = false)
	private Integer paymentStatus = 0;

	@CreationTimestamp
	@Column(name = "create_at", updatable = false)
	private LocalDateTime createAt;
	@UpdateTimestamp
	@Column(name = "update_at")
	private LocalDateTime updateAt;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id", foreignKey = @ForeignKey(name = "fk_orders_shipper_users"))
    private User shipper;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by_vendor_id", length = 36)
    private String assignedByVendorId;
   
	@Builder.Default
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();
	
	@Builder.Default
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderVoucher> orderVouchers = new ArrayList<>();
}