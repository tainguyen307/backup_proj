package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "order_itemID", length = 36)
	private String orderItemID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "orderID", foreignKey = @ForeignKey(name = "fk_orderitems_orders"))
	private Order order;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "productID", foreignKey = @ForeignKey(name = "fk_orderitems_products"))
	private Product product;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;
	
	@Transient
	private BigDecimal itemTotal;
	
	 // ✅ Số tiền chiết khấu (sàn thu)
    @Column(name = "commission_amount", precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    // ✅ Thành tiền thực nhận của vendor (sau khi trừ chiết khấu)
    @Column(name = "net_total", precision = 12, scale = 2)
    private BigDecimal netTotal;
}