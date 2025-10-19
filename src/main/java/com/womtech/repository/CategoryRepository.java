package com.womtech.repository;

import com.womtech.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
	Optional<Category> findByName(String name);

	boolean existsByName(String name);

	List<Category> findByStatus(Integer status);

	List<Category> findAllByOrderByNameAsc();
}
