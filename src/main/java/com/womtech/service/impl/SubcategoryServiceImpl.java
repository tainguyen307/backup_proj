package com.womtech.service.impl;

import com.womtech.entity.Category;
import com.womtech.entity.Subcategory;
import com.womtech.repository.SubcategoryRepository;
import com.womtech.service.SubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubcategoryServiceImpl implements SubcategoryService {

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Override
    public List<Subcategory> getAllSubcategories() {
        return subcategoryRepository.findAll();
    }

    @Override
    public List<Subcategory> getActiveSubcategories() {
        return subcategoryRepository.findByStatus(1);
    }

    @Override
    public List<Subcategory> getSubcategoriesByCategory(Category category) {
        return subcategoryRepository.findByCategory(category);
    }

    @Override
    public List<Subcategory> getSubcategoriesByCategoryId(String categoryID) {
        return subcategoryRepository.findByCategoryCategoryID(categoryID);
    }

    @Override
    public Optional<Subcategory> getSubcategoryById(String id) {
        return subcategoryRepository.findById(id);
    }

    @Override
    public Subcategory saveSubcategory(Subcategory subcategory) {
    	if (subcategory.getSubcategoryID() != null && subcategory.getSubcategoryID().trim().isEmpty()) {
    		subcategory.setSubcategoryID(null); // <- ép về null để Hibernate tự sinh UUID
        }
        return subcategoryRepository.save(subcategory);
    }

    @Override
    public void deleteSubcategory(String id) {
        subcategoryRepository.deleteById(id);
    }

    @Override
    public long getTotalCount() {
        return subcategoryRepository.count();
    }
}
