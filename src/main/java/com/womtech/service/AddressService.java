package com.womtech.service;

import java.util.List;
import java.util.Optional;

import com.womtech.entity.Address;
import com.womtech.entity.User;

public interface AddressService extends BaseService<Address, String> {

	Optional<Address> findByUserAndIsDefaultTrue(User user);

	List<Address> findByUser(User user);
	
	/**
	 * Đặt một địa chỉ của user thành địa chỉ mặc định.
	 * Đồng thời gỡ bỏ mặc định cho các địa chỉ khác của user.
	 */
	void setDefaultAddress(Address address);
}