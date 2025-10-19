package com.womtech.repository;

import com.womtech.entity.Order;
import com.womtech.entity.OrderItem;

import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    
    @Query("""
		    SELECT COUNT(oi) > 0 FROM OrderItem oi
		    WHERE oi.product.productID = :productId
		      AND oi.order.user.userID = :userId
		      AND oi.order.status = 1
		""")
    boolean hasUserPurchasedProduct(@Param("userId") String userId, @Param("productId") String productId);
    
    List<OrderItem> findByOrder(Order order);

	@Query("SELECT oi FROM OrderItem oi WHERE oi.product.ownerUser.userID = :vendorId")
	List<OrderItem> findOrderItemsByVendorId(@Param("vendorId") String vendorId);

	@Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderID = :orderId AND oi.product.ownerUser.userID = :vendorId")
	List<OrderItem> findOrderItemsByOrderIdAndVendorId(@Param("orderId") String orderId,
			@Param("vendorId") String vendorId);

	// Doanh thu theo danh mục (qua SubCategory → Category)
	@Query("SELECT c.name, SUM(oi.price * oi.quantity) " + "FROM OrderItem oi " + "JOIN oi.product p "
			+ "LEFT JOIN p.subcategory sc " + "LEFT JOIN sc.category c "
			+ "WHERE oi.product.ownerUser.userID = :vendorId " + "AND oi.order.createAt BETWEEN :start AND :end "
			+ "AND oi.status = 6 " + "GROUP BY c.name " + "ORDER BY SUM(oi.price * oi.quantity) DESC")
	List<Object[]> findRevenueByCategoryAndVendor(@Param("vendorId") String vendorId,
			@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	// Query cho top sản phẩm bán chạy
	@Query("SELECT p.name, SUM(oi.quantity) " + "FROM OrderItem oi " + "JOIN oi.product p "
			+ "WHERE oi.product.ownerUser.userID = :vendorId " + "AND oi.order.createAt BETWEEN :start AND :end "
			+ "AND oi.status = 6 " + "GROUP BY p.name, p.productID " + "ORDER BY SUM(oi.quantity) DESC")
	List<Object[]> findTopProductsByVendor(@Param("vendorId") String vendorId, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end, Pageable pageable);

	// Query cho tổng doanh thu
	@Query("SELECT SUM(oi.price * oi.quantity) " + "FROM OrderItem oi "
			+ "WHERE oi.product.ownerUser.userID = :vendorId " + "AND oi.order.createAt BETWEEN :start AND :end "
			+ "AND oi.status = 6")
	BigDecimal calculateTotalRevenueByVendor(@Param("vendorId") String vendorId, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	// Query cho tổng số đơn hàng
	@Query("SELECT COUNT(DISTINCT oi.order) " + "FROM OrderItem oi " + "WHERE oi.product.ownerUser.userID = :vendorId "
			+ "AND oi.order.createAt BETWEEN :start AND :end " + "AND oi.status = 6")
	Long countDistinctOrdersByVendor(@Param("vendorId") String vendorId, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

	// Query cho đếm đơn hàng theo trạng thái
	@Query("SELECT o.status, COUNT(DISTINCT oi.order) " + "FROM OrderItem oi " + "JOIN oi.order o "
			+ "WHERE oi.product.ownerUser.userID = :vendorId " + "AND oi.order.createAt BETWEEN :start AND :end "
			+ "GROUP BY o.status")
	List<Object[]> countOrdersByStatus(@Param("vendorId") String vendorId, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);

}
