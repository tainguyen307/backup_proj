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
	@Query("SELECT DATE(oi.order.createAt), SUM(oi.price * oi.quantity) " + "FROM OrderItem oi "
			+ "WHERE oi.product.ownerUser.userID = :vendorId " + "AND oi.order.createAt BETWEEN :start AND :end "
			+ "AND oi.order.paymentStatus = 1 " + "GROUP BY DATE(oi.order.createAt) "
			+ "ORDER BY DATE(oi.order.createAt)")
	List<Object[]> findDailyRevenueByVendorAndPeriod(@Param("vendorId") String vendorId,
			@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	/** Lấy tất cả đơn do một shipper phụ trách (mới nhất trước) */
	List<Order> findByShipper_UserIDOrderByCreateAtDesc(String shipperId);

	/** Lấy đơn của shipper theo trạng thái */
	List<Order> findByShipper_UserIDAndStatusOrderByCreateAtDesc(String shipperId, Integer status);

	/** Lấy các đơn của vendor CHƯA gán shipper */
	@Query("""
			   SELECT o FROM Order o
			   JOIN o.items oi
			   WHERE oi.product.ownerUser.userID = :vendorId
			     AND o.shipper IS NULL
			   ORDER BY o.createAt DESC
			""")
	List<Order> findUnassignedOrdersByVendor(@Param("vendorId") String vendorId);

	/** Lấy các đơn của vendor đã gán cho một shipper cụ thể */
	@Query("""
			   SELECT o FROM Order o
			   JOIN o.items oi
			   WHERE oi.product.ownerUser.userID = :vendorId
			     AND o.shipper.userID = :shipperId
			   ORDER BY o.createAt DESC
			""")
	List<Order> findOrdersByVendorAndShipper(@Param("vendorId") String vendorId, @Param("shipperId") String shipperId);

	// (tuỳ chọn) phiên bản có phân trang:
	Page<Order> findByShipper_UserID(String shipperId, Pageable pageable);

	Page<Order> findByShipper_UserIDAndStatus(String shipperId, Integer status, Pageable pageable);

	// Đếm số đơn của shipper theo trạng thái (dùng cho dashboard/stats)
	Long countByShipper_UserIDAndStatus(String shipperId, Integer status);

	@Query("""
			SELECT o FROM Order o
			WHERE o.shipper.userID = :shipperId
			  AND o.createAt BETWEEN :start AND :end
			ORDER BY o.createAt DESC
			""")
	List<Order> findByShipperAndCreatedBetween(@Param("shipperId") String shipperId,
			@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

	@Query(value = """
			    SELECT DATE(o.update_at) AS d,
			           SUM(CASE WHEN o.status = :delivered THEN 1 ELSE 0 END) AS delivered,
			           SUM(CASE WHEN o.status = :returned  THEN 1 ELSE 0 END) AS returned
			    FROM orders o
			    WHERE o.shipper_id = :shipperId
			      AND o.update_at BETWEEN :start AND :end
			    GROUP BY DATE(o.update_at)
			    ORDER BY d
			""", nativeQuery = true)
	List<Object[]> dailyDeliveredReturned(@Param("shipperId") String shipperId,
			@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end,
			@Param("delivered") Integer delivered, @Param("returned") Integer returned);
}