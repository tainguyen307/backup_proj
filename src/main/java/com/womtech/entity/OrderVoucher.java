package com.womtech.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
	    name = "order_voucher",
	    uniqueConstraints = @UniqueConstraint(columnNames = {"orderID", "voucherID"})
	)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderVoucher {
	@Builder.Default
	@EmbeddedId
	private OrderVoucherID id = new OrderVoucherID();

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("orderID")
	@JoinColumn(name = "orderID", foreignKey = @ForeignKey(name = "fk_order_voucher_order"), nullable = false)
	@JsonIgnore
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("voucherID")
	@JoinColumn(name = "voucherID", foreignKey = @ForeignKey(name = "fk_order_voucher_voucher"), nullable = false)
	private Voucher voucher;

	@Builder.Default
	@Column(name = "discount_value")
	private BigDecimal discountValue = BigDecimal.ZERO;
}
