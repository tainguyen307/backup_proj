package com.womtech.service;

import com.womtech.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ShipperService {

	/** Thống kê nhanh cho dashboard */
	Map<String, Object> getDashboardCounters(String shipperId);

	/** Đơn mới nhất đã gán cho shipper (để hiển thị “Gần đây”) */
	List<Order> getRecentOrders(String shipperId, int limit);

	/**
	 * Danh sách deliveries (phân trang + lọc cơ bản) - status: null => tất cả -
	 * from/to: lọc theo createAt - search: orderID | tên | sđt (lọc ở tầng service)
	 */
	Page<Order> findDeliveries(String shipperId, Integer status, LocalDate fromDate, LocalDate toDate, String search,
			Pageable pageable);

	/** Shipper cập nhật trạng thái đơn */
	void updateOrderStatus(String shipperId, String orderId, Integer newStatus);

	/** Thống kê cá nhân (tổng + chuỗi theo ngày) cho màn /shipper/stats */
	Map<String, Object> personalStats(String shipperId, LocalDate fromDate, LocalDate toDate);
}
