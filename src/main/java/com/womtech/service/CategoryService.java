package com.womtech.service;

import com.womtech.entity.Category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    List<Category> getAllCategories();

    List<Category> getActiveCategories();

    List<Category> getCategoriesSortedByName();

    Optional<Category> getCategoryById(String id);

    Category saveCategory(Category category);

    void deleteCategory(String id);

    boolean existsByName(String name);

    long getTotalCount();
    List<Category> findAll();
    
    Page<Category> getAllCategories(Pageable pageable);
}
