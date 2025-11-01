package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "commissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Double rate; // Tỷ lệ % đã áp dụng tại thời điểm order

    @Column(nullable = false)
    private Double amount; // Số tiền chiết khấu = rate% * subtotal
    
	@CreationTimestamp
	@Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", nullable = false)
	private User vendor; // hoặc Vendor entity riêng nếu bạn có


}