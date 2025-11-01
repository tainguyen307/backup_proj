package com.womtech.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
	    name = "cart_voucher",
	    uniqueConstraints = @UniqueConstraint(columnNames = {"cartID", "voucherID"})
	)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartVoucher {
	@Builder.Default
	@EmbeddedId
	private CartVoucherID id = new CartVoucherID();

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("cartID")
	@JoinColumn(name = "cartID", foreignKey = @ForeignKey(name = "fk_cart_voucher_cart"), nullable = false)
	@JsonIgnore
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("voucherID")
	@JoinColumn(name = "voucherID", foreignKey = @ForeignKey(name = "fk_cart_voucher_voucher"), nullable = false)
	private Voucher voucher;

	@Builder.Default
	@Column(name = "discount_value")
	private BigDecimal discountValue = BigDecimal.ZERO;
}
