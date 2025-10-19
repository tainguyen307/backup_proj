package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "products", indexes = @Index(name = "idx_products_name", columnList = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "productID", length = 36)
	private String productID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "subcategoryID", foreignKey = @ForeignKey(name = "fk_products_subcategories"))
	private Subcategory subcategory;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "brandID", foreignKey = @ForeignKey(name = "fk_products_brands"))
	private Brand brand;
	
	@ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", foreignKey = @ForeignKey(name = "fk_products_owner_user"))
    private User ownerUser; 
	
	private String thumbnail;

	@Column(nullable = false, length = 200)
	private String name;

	@Builder.Default
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price = BigDecimal.ZERO;

	@Column(precision = 12, scale = 2)
	private BigDecimal discount_price;

	@Lob
	@Column(columnDefinition = "MEDIUMTEXT")
	private String description;

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
	@OneToMany(mappedBy = "product")
	private List<Specification> specifications = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "product")
	private List<Inventory> inventories = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "product")
	private List<Review> reviews = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "product")
	private List<CartItem> cartItems = new ArrayList<>();
	@Builder.Default
	@OneToMany(mappedBy = "product")
	private List<OrderItem> orderItems = new ArrayList<>();
}