package com.womtech.service.impl;

import com.womtech.entity.Address;
import com.womtech.entity.User;
import com.womtech.repository.AddressRepository;
import com.womtech.service.AddressService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddressServiceImpl extends BaseServiceImpl<Address, String> implements AddressService {
	@Autowired
	AddressRepository addressRepository;
	
	public AddressServiceImpl(AddressRepository repo) {
		super(repo);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Address> findByUserAndIsDefaultTrue(User user) {
		return addressRepository.findByUserAndIsDefaultTrue(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Address> findByUser(User user) {
		return addressRepository.findByUser(user);
	}

	@Override
	public void setDefaultAddress(Address address) {
		addressRepository.unsetDefaultForUser(address.getUser());
		
		address.setDefault(true);
		addressRepository.save(address);
	}
}