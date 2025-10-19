package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "subcategories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subcategory {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "subcategoryID", length = 36)
	private String subcategoryID;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "categoryID", foreignKey = @ForeignKey(name = "fk_subcategories_categories"))
	private Category category;

	@Column(nullable = false, length = 150)
	private String name;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;

	@Builder.Default
	@OneToMany(mappedBy = "subcategory")
	private List<Product> products = new ArrayList<>();
}