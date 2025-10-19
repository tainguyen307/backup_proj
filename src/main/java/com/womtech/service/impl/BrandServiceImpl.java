package com.womtech.service.impl;

import com.womtech.entity.Brand;
import com.womtech.entity.Product;
import com.womtech.entity.Subcategory;
import com.womtech.repository.BrandRepository;
import com.womtech.service.BrandService;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

	@Autowired
	private BrandRepository brandRepository;

	@Override
	public List<Brand> getAllBrands() {
		return brandRepository.findAll();
	}
	
	@Override
	public Page<Brand> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

	@Override
	public List<Brand> getActiveBrands() {
		return brandRepository.findByStatus(1);
	}

	@Override
	public Optional<Brand> getBrandById(String id) {
		return brandRepository.findById(id);
	}

	@Override
	public Brand saveBrand(Brand brand) {
		if (brand.getBrandID() != null && brand.getBrandID().trim().isEmpty()) {
			brand.setBrandID(null); // <- ép về null để Hibernate tự sinh UUID
		}
		return brandRepository.save(brand);
	}

	@Transactional
	@Override
	public void deleteBrand(String id) {
		System.out.println("Bắt đầu xóa thương hiệu với ID: " + id);

		Optional<Brand> brandOpt = brandRepository.findById(id);
		if (brandOpt.isPresent()) {
			Brand brand = brandOpt.get();

			// Khởi tạo danh sách products
			Hibernate.initialize(brand.getProducts());

			// Kiểm tra xem có sản phẩm nào liên kết không
			if (brand.getProducts() != null && !brand.getProducts().isEmpty()) {
				String brandName = brand.getName() != null ? brand.getName() : "Unknown Brand";
				throw new RuntimeException(
						"Không thể xóa thương hiệu '" + brandName + "' vì vẫn còn sản phẩm liên kết.");
			}
			brandRepository.delete(brand);
		} else {
			throw new RuntimeException("Không tìm thấy thương hiệu có ID: " + id);
		}
	}

	@Override
	public long getTotalCount() {
		return brandRepository.count();
	}
}
