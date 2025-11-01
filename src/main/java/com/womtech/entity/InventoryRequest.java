package com.womtech.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết đến sản phẩm mà vendor sở hữu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_inventory_request_product"))
    private Product product;

    // Số lượng yêu cầu nhập kho
    @Column(nullable = false)
    private Integer quantity;

    // Ghi chú từ vendor (và có thể admin thêm sau)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String note;

    // Trạng thái yêu cầu
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    // Người gửi yêu cầu (Vendor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_inventory_request_user"))
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum trạng thái
    public enum RequestStatus {
        PENDING,   // Chờ xử lý
        APPROVED,  // Đã duyệt (admin sẽ cập nhật inventory thủ công)
        REJECTED   // Từ chối
    }
}