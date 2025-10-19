package com.womtech.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.womtech.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
	@Query("""
		    SELECT p FROM Post p
		    JOIN p.user u
		    WHERE (:role = 'ADMIN' OR u.id = :userId)
		      AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
		      AND (:status IS NULL OR p.status = :status)
		    ORDER BY p.createAt DESC
		    """)
		Page<Post> searchPosts(@Param("userId") String userId,
		                       @Param("role") String role,
		                       @Param("title") String title,
		                       @Param("status") Integer status,
		                       Pageable pageable);

	    @Query("SELECT p FROM Post p WHERE p.status = 1 ORDER BY p.createAt DESC")
	    Page<Post> findAllActive(Pageable pageable);
	    @Query("SELECT p FROM Post p WHERE p.status = 1 ORDER BY p.createAt DESC")
	    List<Post> findAllActiveList();
}