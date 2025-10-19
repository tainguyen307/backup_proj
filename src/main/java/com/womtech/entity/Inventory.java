package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventories", uniqueConstraints = @UniqueConstraint(name = "uk_inventories_prod_loc", columnNames = {
		"productID", "locationID" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "inventoryID", length = 36)
	private String inventoryID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "productID", foreignKey = @ForeignKey(name = "fk_inventories_products"))
	private Product product;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "locationID", foreignKey = @ForeignKey(name = "fk_inventories_locations"))
	private Location location;

	@Builder.Default
	@Column(nullable = false)
	private Integer quantity = 0;

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