package com.womtech.repository;

import com.womtech.entity.InventoryRequest;
import com.womtech.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRequestRepository extends JpaRepository<InventoryRequest, Long> {
    
    // Tìm tất cả yêu cầu của một vendor, sắp xếp theo ngày tạo mới nhất
    List<InventoryRequest> findByUserOrderByCreatedAtDesc(User user);
    
    // Tìm yêu cầu theo trạng thái
    List<InventoryRequest> findByStatusOrderByCreatedAtDesc(InventoryRequest.RequestStatus status);
    
    // Đếm số yêu cầu đang chờ duyệt
    long countByStatus(InventoryRequest.RequestStatus status);
    
    // Sắp xếp theo ngày tạo mới nhất
    List<InventoryRequest> findAllByOrderByCreatedAtDesc();
    
    // Đếm số yêu cầu pending của vendor
    long countByUserAndStatus(User user, InventoryRequest.RequestStatus status);
}