package com.womtech.service.impl;

import com.womtech.entity.Category;
import com.womtech.entity.Product;
import com.womtech.entity.Subcategory;
import com.womtech.repository.CategoryRepository;
import com.womtech.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatus(1);
    }

    @Override
    public List<Category> getCategoriesSortedByName() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category saveCategory(Category category) {
    	if (category.getCategoryID() != null && category.getCategoryID().trim().isEmpty()) {
            category.setCategoryID(null); // <- ép về null để Hibernate tự sinh UUID
        }
        return categoryRepository.save(category);
    }
    @Override
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại"));

        for (Subcategory sub : category.getSubcategories()) {
            for (Product p : sub.getProducts()) {
                if (!p.getCartItems().isEmpty()) {
                    throw new RuntimeException("Không thể xóa vì sản phẩm '" + p.getName() + "' còn đang nằm trong giỏ hàng.");
                }
            }
        }

        categoryRepository.delete(category);
    }


    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public long getTotalCount() {
        return categoryRepository.count();
    }
    
    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}
