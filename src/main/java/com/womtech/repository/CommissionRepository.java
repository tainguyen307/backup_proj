package com.womtech.repository;

import com.womtech.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Repository cho Commission (dữ liệu đã áp dụng)
@Repository
public interface CommissionRepository extends JpaRepository<Commission, String> {

	@Query("SELECT c FROM Commission c WHERE c.orderItem.orderItemID = :orderItemID")
	Optional<Commission> findByOrderItemId(@Param("orderItemID") String orderItemID);

	// Tính tổng doanh thu commission trong khoảng thời gian
	@Query("SELECT SUM(c.amount) FROM Commission c WHERE c.createAt BETWEEN :startDate AND :endDate")
	Double calculateTotalCommissionByDateRange(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	// Lấy danh sách commission trong khoảng thời gian
	@Query("SELECT c FROM Commission c WHERE c.createAt BETWEEN :startDate AND :endDate ORDER BY c.createAt DESC")
	List<Commission> findCommissionsByDateRange(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	// Thống kê commission theo tháng
	@Query("SELECT FUNCTION('YEAR', c.createAt) as year, FUNCTION('MONTH', c.createAt) as month, SUM(c.amount) as total "
			+ "FROM Commission c GROUP BY FUNCTION('YEAR', c.createAt), FUNCTION('MONTH', c.createAt) "
			+ "ORDER BY year DESC, month DESC")
	List<Object[]> getMonthlyCommissionReport();

	@Query("""
			    SELECT
			        c.orderItem.product.ownerUser.username AS vendorName,
			        COUNT(DISTINCT c.orderItem.order.id) AS totalOrders,
			        SUM(c.amount) AS totalCommission,
			        SUM(c.orderItem.netTotal) AS netRevenue
			    FROM Commission c
			    WHERE c.createAt BETWEEN :start AND :end
			      AND c.orderItem.order.paymentStatus = 1
			    GROUP BY c.orderItem.product.ownerUser.username
			    ORDER BY SUM(c.amount) DESC
			""")
	List<Object[]> getCommissionReport(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
