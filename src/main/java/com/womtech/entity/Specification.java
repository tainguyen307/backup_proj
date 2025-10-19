package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "specifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specification {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "specificationID", length = 36)
	private String specificationID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "productID", foreignKey = @ForeignKey(name = "fk_specifications_products"))
	private Product product;

	@Column(nullable = false, length = 150)
	private String name;
	@Column(nullable = false, length = 255)
	private String value;
}