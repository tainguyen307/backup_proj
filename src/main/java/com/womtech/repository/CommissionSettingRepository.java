package com.womtech.repository;

import com.womtech.entity.CommissionSetting;
import com.womtech.enums.CommissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Repository cho CommissionSetting
@Repository
public interface CommissionSettingRepository extends JpaRepository<CommissionSetting, String> {
    
    // Tìm setting theo type và referenceId
    Optional<CommissionSetting> findByTypeAndReferenceId(CommissionType type, String referenceId);
    
    // Tìm Global setting
    Optional<CommissionSetting> findByType(CommissionType type);
    
    // Lấy tất cả settings theo type
    List<CommissionSetting> findAllByType(CommissionType type);
    
    // Tìm setting cho subcategory
    @Query("SELECT cs FROM CommissionSetting cs WHERE cs.type = 'SUBCATEGORY' AND cs.referenceId = :subcategoryId")
    Optional<CommissionSetting> findBySubcategoryId(@Param("subcategoryId") String subcategoryId);
    
    // Tìm setting cho category
    @Query("SELECT cs FROM CommissionSetting cs WHERE cs.type = 'CATEGORY' AND cs.referenceId = :categoryId")
    Optional<CommissionSetting> findByCategoryId(@Param("categoryId") String categoryId);
}