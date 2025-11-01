package com.womtech.service;

import com.womtech.entity.InventoryRequest;
import com.womtech.entity.Product;
import com.womtech.entity.User;
import com.womtech.repository.InventoryRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryRequestService {

    @Autowired
    private InventoryRequestRepository inventoryRequestRepository;

    /**
     * Tạo yêu cầu nhập kho mới
     */
    @Transactional
    public InventoryRequest createRequest(Product product, int quantity, String note, User user) {
        // Validation
        if (product == null) {
            throw new IllegalArgumentException("Sản phẩm không được để trống");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (!product.getOwnerUser().getUserID().equals(user.getUserID())) {
            throw new IllegalArgumentException("Bạn không có quyền tạo yêu cầu cho sản phẩm này");
        }

        InventoryRequest request = InventoryRequest.builder()
                .product(product)
                .quantity(quantity)
                .note(note)
                .user(user)
                .status(InventoryRequest.RequestStatus.PENDING)
                .build();

        return inventoryRequestRepository.save(request);
    }

    /**
     * Cập nhật trạng thái yêu cầu (chỉ đổi status, không cập nhật inventory)
     */
    @Transactional
    public void updateRequestStatus(Long requestId, InventoryRequest.RequestStatus newStatus, String adminNote) {
        InventoryRequest request = inventoryRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

        if (request.getStatus() != InventoryRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu đã được xử lý trước đó");
        }

        request.setStatus(newStatus);
        
        // Nếu có admin note, thêm vào note hiện tại
        if (adminNote != null && !adminNote.isEmpty()) {
            String currentNote = request.getNote() != null ? request.getNote() : "";
            request.setNote(currentNote + "\n[Admin]: " + adminNote);
        }
        
        inventoryRequestRepository.save(request);
    }

    /**
     * Lấy danh sách yêu cầu của vendor
     */
    public List<InventoryRequest> getRequestsByUser(User user) {
        return inventoryRequestRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Lấy tất cả yêu cầu
     */
    public List<InventoryRequest> getAllRequests() {
        return inventoryRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Lấy yêu cầu theo trạng thái
     */
    public List<InventoryRequest> getRequestsByStatus(InventoryRequest.RequestStatus status) {
        return inventoryRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Đếm số yêu cầu đang chờ duyệt
     */
    public long countPendingRequests() {
        return inventoryRequestRepository.countByStatus(InventoryRequest.RequestStatus.PENDING);
    }
    
    /**
     * Đếm số yêu cầu pending của vendor
     */
    public long countUserPendingRequests(User user) {
        return inventoryRequestRepository.countByUserAndStatus(user, InventoryRequest.RequestStatus.PENDING);
    }
}