package com.womtech.service;

import com.womtech.entity.Brand;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    List<Brand> getAllBrands();
    List<Brand> getActiveBrands();
    Optional<Brand> getBrandById(String id);
    Brand saveBrand(Brand brand);
    void deleteBrand(String id);
    long getTotalCount();
    Page<Brand> getAllBrands(Pageable pageable);
}
