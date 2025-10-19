package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
	@Id 	
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "categoryID", length = 36)
	private String categoryID;

	@Column(nullable = false, length = 150)
	private String name;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;

	@Builder.Default
	@OneToMany(mappedBy = "category")
	private List<Subcategory> subcategories = new ArrayList<>();
	
	
}