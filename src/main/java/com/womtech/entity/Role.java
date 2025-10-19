package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "roleID", length = 36)
	private String roleID;

	@Column(nullable = false, length = 100)
	private String rolename;

	@Column(length = 255)
	private String description;

	@Builder.Default
	@OneToMany(mappedBy = "role")
	private List<User> users = new ArrayList<>();
}