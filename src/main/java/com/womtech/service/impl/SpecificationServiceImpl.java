package com.womtech.service.impl;

import com.womtech.entity.Specification;
import com.womtech.entity.Product;
import com.womtech.repository.SpecificationRepository;
import com.womtech.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationRepository specificationRepository;

    @Override
    public List<Specification> getAllSpecifications() {
        return specificationRepository.findAll();
    }
    public Page<Specification> getAllSpecifications(Pageable pageable) {
        return specificationRepository.findAll(pageable);
    }

    @Override
    public List<Specification> getSpecificationsByProduct(Product product) {
        return specificationRepository.findByProduct(product);
    }

    @Override
    public List<Specification> getSpecificationsByProductID(String productId) {
        return specificationRepository.findByProductProductID(productId);
    }

    @Override
    public Optional<Specification> getSpecificationByID(String id) {
        return specificationRepository.findById(id);
    }

    @Override
    public Specification saveSpecification(Specification specification) {
    	if (specification.getSpecificationID() != null && specification.getSpecificationID().trim().isEmpty()) {
    		specification.setSpecificationID(null);
        }
        return specificationRepository.save(specification);
    }

    @Override
    public void deleteSpecification(String id) {
        specificationRepository.deleteById(id);
    }

    @Override
    public long getTotalCount() {
        return specificationRepository.count();
    }
}
