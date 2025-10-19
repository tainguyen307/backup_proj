package com.womtech.repository;

import com.womtech.entity.Specification;
import com.womtech.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecificationRepository extends JpaRepository<Specification, String> {
    List<Specification> findByProduct(Product product);
    List<Specification> findByProductProductID(String productID);
}
