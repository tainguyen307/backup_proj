package com.womtech.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import com.womtech.entity.Role;
import com.womtech.service.RoleService;

@Service
public class RoleServiceImpl extends BaseServiceImpl<Role, String> implements RoleService {
	public RoleServiceImpl(JpaRepository<Role, String> repo) {
		super(repo);
	}
}