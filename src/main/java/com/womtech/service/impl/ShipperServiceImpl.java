package com.womtech.service.impl;

import com.womtech.entity.Order;
import com.womtech.repository.OrderRepository;
import com.womtech.service.ShipperService;
import com.womtech.util.OrderStatusHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShipperServiceImpl implements ShipperService {

	@Autowired
	private OrderRepository orderRepository;

	// ===== Helpers nhóm trạng thái cho shipper =====
	private boolean isAssignedBucket(Integer s) {
		if (s == null)
			return false;
		// Chỉnh theo app bạn: CONFIRMED, PREPARING, PACKED
		return s.equals(OrderStatusHelper.STATUS_CONFIRMED) || s.equals(OrderStatusHelper.STATUS_PREPARING)
				|| s.equals(OrderStatusHelper.STATUS_PACKED);
	}

	private boolean isInTransit(Integer s) {
		return s != null && s.equals(OrderStatusHelper.STATUS_SHIPPED);
	}

	private boolean isDelivered(Integer s) {
		return s != null && s.equals(OrderStatusHelper.STATUS_DELIVERED);
	}

	private boolean isFailed(Integer s) {
		// thất bại = huỷ hoặc hoàn
		return s != null
				&& (s.equals(OrderStatusHelper.STATUS_CANCELLED) || s.equals(OrderStatusHelper.STATUS_RETURNED));
	}

	private boolean isCOD(Order o) {
		if (o == null || o.getPaymentMethod() == null)
			return false;
		String pm = o.getPaymentMethod().trim().toUpperCase();
		return pm.contains("COD") || pm.contains("CASH");
	}

	// ================== Dashboard ==================
	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getDashboardCounters(String shipperId) {
		List<Order> orders = orderRepository.findByShipper_UserIDOrderByCreateAtDesc(shipperId);

		long assigned = 0, inTransit = 0, delivered = 0, failed = 0;
		BigDecimal codToCollect = BigDecimal.ZERO;
		BigDecimal codCollected = BigDecimal.ZERO;

		for (Order o : orders) {
			Integer st = o.getStatus();

			if (isAssignedBucket(st))
				assigned++;
			else if (isInTransit(st))
				inTransit++;
			else if (isDelivered(st))
				delivered++;
			else if (isFailed(st))
				failed++;

			if (isCOD(o) && o.getTotalPrice() != null) {
				if (o.getPaymentStatus() != null && o.getPaymentStatus() == 1) {
					codCollected = codCollected.add(o.getTotalPrice());
				} else {
					codToCollect = codToCollect.add(o.getTotalPrice());
				}
			}
		}

		Map<String, Object> rs = new HashMap<>();
		rs.put("assignedCount", assigned);
		rs.put("inTransitCount", inTransit);
		rs.put("deliveredCount", delivered);
		rs.put("failedCount", failed);
		rs.put("codToCollect", codToCollect);
		rs.put("codCollected", codCollected);
		return rs;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> getRecentOrders(String shipperId, int limit) {
		List<Order> list = orderRepository.findByShipper_UserIDOrderByCreateAtDesc(shipperId);
		int lim = limit <= 0 ? 5 : limit;
		return list.stream().limit(lim).collect(Collectors.toList());
	}

	// ================== Deliveries list ==================
	@Override
	@Transactional(readOnly = true)
	public Page<Order> findDeliveries(String shipperId, Integer status, LocalDate fromDate, LocalDate toDate,
			String search, Pageable pageable) {

		Page<Order> page;
		if (status == null) {
			page = orderRepository.findByShipper_UserID(shipperId, pageable);
		} else {
			page = orderRepository.findByShipper_UserIDAndStatus(shipperId, status, pageable);
		}

		List<Order> filtered = new ArrayList<>(page.getContent());

		if (fromDate != null || toDate != null) {
			LocalDateTime from = (fromDate != null) ? fromDate.atStartOfDay() : LocalDateTime.MIN;
			LocalDateTime to = (toDate != null) ? toDate.atTime(LocalTime.MAX) : LocalDateTime.MAX;
			filtered = filtered.stream().filter(
					o -> o.getCreateAt() != null && !o.getCreateAt().isBefore(from) && !o.getCreateAt().isAfter(to))
					.collect(Collectors.toList());
		}

		if (search != null && !search.isBlank()) {
			String q = search.trim().toLowerCase();
			filtered = filtered.stream().filter(o -> {
				if (o.getOrderID() != null && o.getOrderID().toLowerCase().contains(q))
					return true;
				if (o.getAddress() != null) {
					if (o.getAddress().getFullname() != null && o.getAddress().getFullname().toLowerCase().contains(q))
						return true;
					if (o.getAddress().getPhone() != null && o.getAddress().getPhone().toLowerCase().contains(q))
						return true;
				}
				return false;
			}).collect(Collectors.toList());
		}

		return new PageImpl<>(filtered, pageable, page.getTotalElements());
	}

	// ================== Update status ==================
	@Override
	public void updateOrderStatus(String shipperId, String orderId, Integer newStatus) {
		Order o = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

		if (o.getShipper() == null || !shipperId.equals(o.getShipper().getUserID())) {
			throw new RuntimeException("Bạn không có quyền cập nhật đơn này.");
		}

		if (isDelivered(o.getStatus()) || isFailed(o.getStatus())) {
			throw new RuntimeException("Đơn đã kết thúc, không thể cập nhật.");
		}

		// (tuỳ chính sách) Cho phép chuyển sang SHIPPED/DELIVERED/RETURNED
		o.setStatus(newStatus);
		o.setUpdateAt(LocalDateTime.now());

		// Nếu là COD và giao thành công => auto mark paid (optional)
		if (isCOD(o) && newStatus != null && newStatus.equals(OrderStatusHelper.STATUS_DELIVERED)) {
			o.setPaymentStatus(1); // 1 = đã thanh toán
		}

		orderRepository.save(o);
	}

	// ================== Stats ==================
	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> personalStats(String shipperId, LocalDate fromDate, LocalDate toDate) {
		LocalDateTime start = (fromDate != null) ? fromDate.atStartOfDay()
				: LocalDate.now().minusDays(30).atStartOfDay();
		LocalDateTime end = (toDate != null) ? toDate.atTime(LocalTime.MAX) : LocalDateTime.now();

		List<Order> all = orderRepository.findByShipper_UserIDOrderByCreateAtDesc(shipperId);

		long assigned = 0, inTransit = 0, delivered = 0, failed = 0;
		for (Order o : all) {
			if (o.getCreateAt() == null || o.getCreateAt().isBefore(start) || o.getCreateAt().isAfter(end))
				continue;
			Integer st = o.getStatus();
			if (isAssignedBucket(st))
				assigned++;
			else if (isInTransit(st))
				inTransit++;
			else if (isDelivered(st))
				delivered++;
			else if (isFailed(st))
				failed++;
		}

		// Chuỗi theo ngày (delivered/returned) — tính in-memory
		Map<LocalDate, long[]> bucket = new TreeMap<>();
		for (Order o : all) {
			if (o.getCreateAt() == null)
				continue;
			if (o.getCreateAt().isBefore(start) || o.getCreateAt().isAfter(end))
				continue;

			LocalDate d = o.getCreateAt().toLocalDate();
			bucket.putIfAbsent(d, new long[] { 0, 0 }); // [delivered, returned]

			if (isDelivered(o.getStatus()))
				bucket.get(d)[0]++;
			if (o.getStatus() != null && o.getStatus().equals(OrderStatusHelper.STATUS_RETURNED))
				bucket.get(d)[1]++;
		}

		List<String> dates = new ArrayList<>();
		List<Long> deliveredSeries = new ArrayList<>();
		List<Long> returnedSeries = new ArrayList<>();
		for (var e : bucket.entrySet()) {
			dates.add(e.getKey().toString());
			deliveredSeries.add(e.getValue()[0]);
			returnedSeries.add(e.getValue()[1]);
		}

		Map<String, Object> rs = new HashMap<>();
		rs.put("assigned", assigned);
		rs.put("inTransit", inTransit);
		rs.put("delivered", delivered);
		rs.put("failed", failed);
		rs.put("dates", dates);
		rs.put("deliveredSeries", deliveredSeries);
		rs.put("returnedSeries", returnedSeries);
		return rs;
	}
}
