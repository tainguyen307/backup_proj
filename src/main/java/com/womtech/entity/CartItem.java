package com.womtech.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(name = "uk_cartitem_cart_product", columnNames = {
		"cartID", "productID" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "cart_itemID", length = 36)
	private String cartItemID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "cartID", foreignKey = @ForeignKey(name = "fk_cartitems_cart"))
	private Cart cart;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "productID", foreignKey = @ForeignKey(name = "fk_cartitems_product"))
	private Product product;

	@Builder.Default
	@Column(nullable = false)
	private Integer quantity = 1;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;
	
	@Transient
	private BigDecimal itemTotal;
}