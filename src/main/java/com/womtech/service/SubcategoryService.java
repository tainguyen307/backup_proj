package com.womtech.service;

import com.womtech.entity.Category;
import com.womtech.entity.Subcategory;

import java.util.List;
import java.util.Optional;

public interface SubcategoryService {

    List<Subcategory> getAllSubcategories();

    List<Subcategory> getActiveSubcategories();

    List<Subcategory> getSubcategoriesByCategory(Category category);

    List<Subcategory> getSubcategoriesByCategoryId(String categoryID);

    Optional<Subcategory> getSubcategoryById(String id);

    Subcategory saveSubcategory(Subcategory subcategory);

    void deleteSubcategory(String id);

    long getTotalCount();
}
