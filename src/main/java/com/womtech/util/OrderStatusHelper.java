package com.womtech.util;

import java.util.HashMap;
import java.util.Map;

public class OrderStatusHelper {
    
    // ============ ORDER STATUS CONSTANTS ============
    public static final Integer STATUS_CANCELLED = 0;
    public static final Integer STATUS_PENDING = 1;
    public static final Integer STATUS_CONFIRMED = 2;
    public static final Integer STATUS_PREPARING = 3;
    public static final Integer STATUS_PACKED = 4;
    public static final Integer STATUS_SHIPPED = 5;
    public static final Integer STATUS_DELIVERED = 6;
    public static final Integer STATUS_RETURNED = 7;

    // ============ ITEM STATUS CONSTANTS ============
    public static final Integer ITEM_STATUS_CANCELLED = 0;      // Hủy

    public static final Integer ITEM_STATUS_PENDING = 1;        // Chờ xác nhận
    public static final Integer ITEM_STATUS_CONFIRMED = 2;      // Đã xác nhận
    public static final Integer ITEM_STATUS_PREPARING = 3;      // Đang chuẩn bị
    public static final Integer ITEM_STATUS_PACKED = 4;         // Đã đóng gói
    public static final Integer ITEM_STATUS_SHIPPED = 5;        // Đang giao
    public static final Integer ITEM_STATUS_DELIVERED = 6;      // Đã giao
    public static final Integer ITEM_STATUS_RETURNED = 7;       // Hoàn trả
    
    // ============ PAYMENT STATUS CONSTANTS ============
    public static final Integer PAYMENT_PENDING = 0;
    public static final Integer PAYMENT_PAID = 1;
    public static final Integer PAYMENT_FAILED = 2;
    
    // ============ STATUS LABELS ============
    private static final Map<Integer, String> ORDER_STATUS_LABELS = new HashMap<>();
    private static final Map<Integer, String> ITEM_STATUS_LABELS = new HashMap<>();
    private static final Map<Integer, String> PAYMENT_STATUS_LABELS = new HashMap<>();
    
    // ============ STATUS BADGE CLASSES ============
    private static final Map<Integer, String> ORDER_STATUS_BADGES = new HashMap<>();
    private static final Map<Integer, String> ITEM_STATUS_BADGES = new HashMap<>();
    private static final Map<Integer, String> PAYMENT_BADGES = new HashMap<>();
    
    static {
        // Order Status Labels
        ORDER_STATUS_LABELS.put(STATUS_CANCELLED, "Đã hủy");
        ORDER_STATUS_LABELS.put(STATUS_PENDING, "Chờ xác nhận");
        ORDER_STATUS_LABELS.put(STATUS_CONFIRMED, "Đã xác nhận");
        ORDER_STATUS_LABELS.put(STATUS_PREPARING, "Đang chuẩn bị");
        ORDER_STATUS_LABELS.put(STATUS_PACKED, "Đã đóng gói");
        ORDER_STATUS_LABELS.put(STATUS_SHIPPED, "Đang giao");
        ORDER_STATUS_LABELS.put(STATUS_DELIVERED, "Đã giao");
        ORDER_STATUS_LABELS.put(STATUS_RETURNED, "Hoàn trả");
        
        // Item Status Labels (same as order)
        ITEM_STATUS_LABELS.put(ITEM_STATUS_CANCELLED, "Đã hủy");
        ITEM_STATUS_LABELS.put(ITEM_STATUS_PENDING, "Chờ xác nhận");
        ITEM_STATUS_LABELS.put(ITEM_STATUS_CONFIRMED, "Đã xác nhận");
        ITEM_STATUS_LABELS.put(ITEM_STATUS_PREPARING, "Đang chuẩn bị");
        ITEM_STATUS_LABELS.put(ITEM_STATUS_PACKED, "Đã đóng gói");
        ITEM_STATUS_LABELS.put(ITEM_STATUS_SHIPPED, "Đang giao");
        ITEM_STATUS_LABELS.put(ITEM_STATUS_DELIVERED, "Đã giao");
        ITEM_STATUS_LABELS.put(ITEM_STATUS_RETURNED, "Hoàn trả");
        
        // Payment Status Labels
        PAYMENT_STATUS_LABELS.put(PAYMENT_PENDING, "Chờ thanh toán");
        PAYMENT_STATUS_LABELS.put(PAYMENT_PAID, "Đã thanh toán");
        PAYMENT_STATUS_LABELS.put(PAYMENT_FAILED, "Thất bại");
        
        // Order Status Badge Classes
        ORDER_STATUS_BADGES.put(STATUS_CANCELLED, "bg-danger");
        ORDER_STATUS_BADGES.put(STATUS_PENDING, "bg-warning");
        ORDER_STATUS_BADGES.put(STATUS_CONFIRMED, "bg-info");
        ORDER_STATUS_BADGES.put(STATUS_PREPARING, "bg-primary");
        ORDER_STATUS_BADGES.put(STATUS_PACKED, "bg-secondary");
        ORDER_STATUS_BADGES.put(STATUS_SHIPPED, "bg-primary");
        ORDER_STATUS_BADGES.put(STATUS_DELIVERED, "bg-success");
        ORDER_STATUS_BADGES.put(STATUS_RETURNED, "bg-warning");
        
        // Item Status Badge Classes (same as order)
        ITEM_STATUS_BADGES.put(ITEM_STATUS_CANCELLED, "bg-danger");
        ITEM_STATUS_BADGES.put(ITEM_STATUS_PENDING, "bg-warning");
        ITEM_STATUS_BADGES.put(ITEM_STATUS_CONFIRMED, "bg-info");
        ITEM_STATUS_BADGES.put(ITEM_STATUS_PREPARING, "bg-primary");
        ITEM_STATUS_BADGES.put(ITEM_STATUS_PACKED, "bg-secondary");
        ITEM_STATUS_BADGES.put(ITEM_STATUS_SHIPPED, "bg-primary");
        ITEM_STATUS_BADGES.put(ITEM_STATUS_DELIVERED, "bg-success");
        ITEM_STATUS_BADGES.put(ITEM_STATUS_RETURNED, "bg-warning");
        
        // Payment Status Badge Classes
        PAYMENT_BADGES.put(PAYMENT_PENDING, "bg-warning");
        PAYMENT_BADGES.put(PAYMENT_PAID, "bg-success");
        PAYMENT_BADGES.put(PAYMENT_FAILED, "bg-danger");
    }
    
    // ============ ORDER STATUS METHODS ============
    public static String getOrderStatusLabel(Integer status) {
        return ORDER_STATUS_LABELS.getOrDefault(status, "Không xác định");
    }
    
    public static String getOrderStatusBadgeClass(Integer status) {
        return ORDER_STATUS_BADGES.getOrDefault(status, "bg-secondary");
    }
    
    // ============ ITEM STATUS METHODS ============
    public static String getItemStatusLabel(Integer status) {
        return ITEM_STATUS_LABELS.getOrDefault(status, "Không xác định");
    }
    
    public static String getItemStatusBadgeClass(Integer status) {
        return ITEM_STATUS_BADGES.getOrDefault(status, "bg-secondary");
    }
    
    // ============ PAYMENT STATUS METHODS ============
    public static String getPaymentStatusLabel(Integer paymentStatus) {
        return PAYMENT_STATUS_LABELS.getOrDefault(paymentStatus, "Không xác định");
    }
    
    public static String getPaymentBadgeClass(Integer paymentStatus) {
        return PAYMENT_BADGES.getOrDefault(paymentStatus, "bg-secondary");
    }
    
    // ============ BACKWARD COMPATIBILITY ============
    // Keep old method names for existing code
    public static String getStatusLabel(Integer status) {
        return getOrderStatusLabel(status);
    }
    
    public static String getStatusBadgeClass(Integer status) {
        return getOrderStatusBadgeClass(status);
    }
    
    // ============ CONVERSION METHODS ============
    /**
     * Convert Order status to corresponding Item status
     */
    public static Integer orderStatusToItemStatus(Integer orderStatus) {
        if (orderStatus == null) return ITEM_STATUS_PENDING;
        
        switch (orderStatus) {
            case 0: return ITEM_STATUS_CANCELLED;
            case 1: return ITEM_STATUS_PENDING;
            case 2: return ITEM_STATUS_CONFIRMED;
            case 3: return ITEM_STATUS_PREPARING;
            case 4: return ITEM_STATUS_PACKED;
            case 5: return ITEM_STATUS_SHIPPED;
            case 6: return ITEM_STATUS_DELIVERED;
            case 7: return ITEM_STATUS_RETURNED;
            default: return ITEM_STATUS_PENDING;
        }
    }
    
    /**
     * Convert Item status to corresponding Order status
     */
    public static Integer itemStatusToOrderStatus(Integer itemStatus) {
        if (itemStatus == null) return STATUS_PENDING;
        
        switch (itemStatus) {
            case 0: return STATUS_CANCELLED;
            case 1: return STATUS_PENDING;
            case 2: return STATUS_CONFIRMED;
            case 3: return STATUS_PREPARING;
            case 4: return STATUS_PACKED;
            case 5: return STATUS_SHIPPED;
            case 6: return STATUS_DELIVERED;
            case 7: return STATUS_RETURNED;
            default: return STATUS_PENDING;
        }
    }
    
    // ============ VALIDATION METHODS ============
    public static boolean canUpdateOrderStatus(Integer currentStatus, Integer newStatus) {
        if (currentStatus == null || newStatus == null) return false;
        if (currentStatus.equals(STATUS_CANCELLED) || 
            currentStatus.equals(STATUS_DELIVERED) || 
            currentStatus.equals(STATUS_RETURNED)) {
            return false; // Cannot update from terminal states
        }
        
        // Allow sequential progression or cancellation
        return newStatus.equals(currentStatus + 1) || newStatus.equals(STATUS_CANCELLED);
    }
    
    public static boolean canUpdateItemStatus(Integer currentStatus, Integer newStatus) {
        if (currentStatus == null || newStatus == null) return false;
        if (currentStatus.equals(ITEM_STATUS_CANCELLED) || 
            currentStatus.equals(ITEM_STATUS_DELIVERED) || 
            currentStatus.equals(ITEM_STATUS_RETURNED)) {
            return false; // Cannot update from terminal states
        }
        
        // Allow sequential progression or cancellation
        return newStatus.equals(currentStatus + 1) || newStatus.equals(ITEM_STATUS_CANCELLED);
    }
    
    public static Integer getNextOrderStatus(Integer currentStatus) {
        if (currentStatus == null || currentStatus >= STATUS_DELIVERED) {
            return null;
        }
        return currentStatus + 1;
    }
    
    public static Integer getNextItemStatus(Integer currentStatus) {
        if (currentStatus == null || currentStatus >= ITEM_STATUS_DELIVERED) {
            return null;
        }
        return currentStatus + 1;
    }
}
