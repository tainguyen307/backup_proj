package com.womtech.repository;

import com.womtech.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String> {
    List<Brand> findByStatus(Integer status);
}
