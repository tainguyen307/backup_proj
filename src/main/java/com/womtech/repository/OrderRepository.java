package com.womtech.repository;

import com.womtech.entity.Order;
import com.womtech.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
	List<Order> findByUser(User user);
	
	Page<Order> findByUser(User user, Pageable pageable);
	
	Page<Order> findByUserAndStatus(User user, int status, Pageable pageable);
	
	List<Order> findByUserOrderByCreateAtDesc(User user);

	List<Order> findByStatusOrderByCreateAtDesc(Integer status);

	// 1️⃣ Lấy tất cả đơn hàng có chứa sản phẩm thuộc vendor
	@Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.ownerUser.userID = :vendorId ORDER BY o.createAt DESC")
	List<Order> findOrdersByVendorId(@Param("vendorId") String vendorId);

	// 2️⃣ Lấy đơn hàng của vendor theo trạng thái
	@Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.ownerUser.userID = :vendorId AND o.status = :status ORDER BY o.createAt DESC")
	List<Order> findOrdersByVendorIdAndStatus(@Param("vendorId") String vendorId, @Param("status") Integer status);

	// 3️⃣ Lọc theo khoảng thời gian
	@Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.product.ownerUser.userID = :vendorId AND o.createAt BETWEEN :startDate AND :endDate ORDER BY o.createAt DESC")
	List<Order> findOrdersByVendorIdAndDateRange(@Param("vendorId") String vendorId,
			@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	// 4️⃣ Đếm tổng số đơn hàng có sản phẩm thuộc vendor
	@Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items oi WHERE oi.product.ownerUser.userID = :vendorId")
	Long countOrdersByVendorId(@Param("vendorId") String vendorId);

	// 5️⃣ Đếm số đơn hàng theo trạng thái cho vendor
	@Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items oi WHERE oi.product.ownerUser.userID = :vendorId AND oi.status = :status")
	Long countOrdersByVendorIdAndStatus(@Param("vendorId") String vendorId, @Param("status") Integer status);

	// Query cho biểu đồ doanh thu hàng ngày
    @Query("SELECT DATE(oi.order.createAt), SUM(oi.price * oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.product.ownerUser.userID = :vendorId " +
           "AND oi.order.createAt BETWEEN :start AND :end " +
           "AND oi.status = 6 " + 
           "GROUP BY DATE(oi.order.createAt) " +
           "ORDER BY DATE(oi.order.createAt)")
    List<Object[]> findDailyRevenueByVendorAndPeriod(@Param("vendorId") String vendorId, 
                                                    @Param("start") LocalDateTime start, 
                                                    @Param("end") LocalDateTime end);

}