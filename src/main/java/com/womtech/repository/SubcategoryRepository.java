package com.womtech.repository;

import com.womtech.entity.Category;
import com.womtech.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, String> {
    List<Subcategory> findByCategory(Category category);
    List<Subcategory> findByCategoryCategoryID(String categoryID);
    List<Subcategory> findByStatus(Integer status);
}
