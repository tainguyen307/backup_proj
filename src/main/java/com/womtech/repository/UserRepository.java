package com.womtech.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.womtech.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    
    @Query("SELECT u FROM User u " +
    	       "WHERE (:keyword IS NULL OR u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.userID LIKE %:keyword%) " +
    	       "AND (:role IS NULL OR u.role.rolename = :role) " +
    	       "AND (:status IS NULL OR u.status = :status)")
    	Page<User> searchUsers(@Param("keyword") String keyword,
    	                       @Param("role") String role,
    	                       @Param("status") Integer status,
    	                       Pageable pageable);
}