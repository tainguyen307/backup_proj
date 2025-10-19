package com.womtech.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import com.womtech.entity.Location;
import com.womtech.service.LocationService;

@Service
public class LocationServiceImpl extends BaseServiceImpl<Location, String> implements LocationService {
	public LocationServiceImpl(JpaRepository<Location, String> repo) {
		super(repo);
	}
}