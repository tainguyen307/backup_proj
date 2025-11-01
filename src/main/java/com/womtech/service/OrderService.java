package com.womtech.service;

import java.util.List;

import com.womtech.entity.Address;
import com.womtech.entity.Cart;
import com.womtech.entity.Order;
import com.womtech.entity.OrderItem;
import com.womtech.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService extends BaseService<Order, String> {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(String orderId);
    List<Order> getOrdersByUser(User user);
    List<Order> getOrdersByStatus(Integer status);
    List<Order> getOrdersByVendorId(String vendorId);
    List<Order> getOrdersByVendorIdAndStatus(String vendorId, Integer status);
    List<Order> getOrdersByVendorIdAndDateRange(String vendorId, LocalDateTime startDate, LocalDateTime endDate);
    Long countOrdersByVendorId(String vendorId);
    Long countOrdersByVendorIdAndStatus(String vendorId, Integer status);
    List<OrderItem> getOrderItemsByOrderIdAndVendorId(String orderId, String vendorId);
    
    Order saveOrder(Order order);
    void updateOrderStatus(String orderId, Integer newStatus);
    void cancelOrder(String orderId);
    void cancelVendorOrderItems(String orderId, String vendorId);
    Map<String, Object> getVendorOrderStatistics(String vendorId, LocalDateTime startDate, LocalDateTime endDate);
    void deleteOrder(String orderId);
	void updateVendorItemStatus(String orderId, String orderItemId, String vendorId, Integer newItemStatus);
	// Phương thức mới cho biểu đồ doanh thu
    Map<String, Object> getRevenueChartData(String vendorId, LocalDateTime start, LocalDateTime end);
    
    // Phương thức cho biểu đồ phân loại
    Map<String, Object> getCategoryRevenueData(String vendorId, LocalDateTime start, LocalDateTime end);
    
    // Phương thức cho top sản phẩm
    Map<String, Object> getTopProductsData(String vendorId, LocalDateTime start, LocalDateTime end);
    
	List<Order> findByUser(User user);
	int totalQuantity(Order order);
	Page<Order> findByUser(User user, Pageable pageable);
	Page<Order> findByUserAndStatus(User user, int status, Pageable pageable);
	Order createOrderFromCart(Cart cart, Address address, String payment_method, BigDecimal finalPrice);
	BigDecimal totalPrice(Order order);
	
	void assignShipper(String orderId, String shipperId, String vendorId);
	void unassignShipper(String orderId, String vendorId);

	List<Order> getOrdersByShipper(String shipperId);
	List<Order> getOrdersByShipperAndStatus(String shipperId, Integer status);

	List<Order> getUnassignedOrdersByVendor(String vendorId);
	List<Order> getOrdersByVendorAndShipper(String vendorId, String shipperId);
}
