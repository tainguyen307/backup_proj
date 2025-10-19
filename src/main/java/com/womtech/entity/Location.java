package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "locationID", length = 36)
	private String locationID;

	@Column(nullable = false, length = 150)
	private String name;

	@Builder.Default
	@Column(nullable = false)
	private Integer status = 1;

	@Builder.Default
	@OneToMany(mappedBy = "location")
	private List<Inventory> inventories = new ArrayList<>();
}