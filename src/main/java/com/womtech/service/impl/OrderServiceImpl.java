package com.womtech.service.impl;

import com.womtech.entity.Order;
import com.womtech.entity.OrderItem;
import com.womtech.entity.User;
import com.womtech.repository.CartRepository;
import com.womtech.repository.OrderItemRepository;
import com.womtech.repository.OrderRepository;
import com.womtech.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.womtech.entity.Address;
import com.womtech.entity.Cart;
import com.womtech.service.CommissionService;
import com.womtech.service.OrderItemService;
import com.womtech.service.OrderService;
import com.womtech.service.OrderVoucherService;
import com.womtech.util.OrderStatusHelper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class OrderServiceImpl extends BaseServiceImpl<Order, String> implements OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private OrderItemService orderItemService;

	@Autowired
	private OrderVoucherService orderVoucherService;

	@Autowired
	private UserRepository userRepository;

	// 🔥 THÊM COMMISSION SERVICE
	@Autowired
	private CommissionService commissionService;

	public OrderServiceImpl(JpaRepository<Order, String> repo) {
		super(repo);
	}

	@Override
	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public Optional<Order> getOrderById(String orderId) {
		return orderRepository.findById(orderId);
	}

	@Override
	public List<Order> getOrdersByUser(User user) {
		return orderRepository.findByUserOrderByCreateAtDesc(user);
	}

	@Override
	public List<Order> getOrdersByStatus(Integer status) {
		return orderRepository.findByStatusOrderByCreateAtDesc(status);
	}

	@Override
	public List<Order> getOrdersByVendorId(String vendorId) {
		return orderRepository.findOrdersByVendorId(vendorId);
	}

	@Override
	public List<Order> getOrdersByVendorIdAndStatus(String vendorId, Integer status) {
		return orderRepository.findOrdersByVendorIdAndStatus(vendorId, status);
	}

	@Override
	public List<Order> getOrdersByVendorIdAndDateRange(String vendorId, LocalDateTime startDate,
			LocalDateTime endDate) {
		return orderRepository.findOrdersByVendorIdAndDateRange(vendorId, startDate, endDate);
	}

	@Override
	public Long countOrdersByVendorId(String vendorId) {
		return orderRepository.countOrdersByVendorId(vendorId);
	}

	@Override
	public Long countOrdersByVendorIdAndStatus(String vendorId, Integer status) {
		return orderRepository.countOrdersByVendorIdAndStatus(vendorId, status);
	}

	@Override
	public List<OrderItem> getOrderItemsByOrderIdAndVendorId(String orderId, String vendorId) {
		return orderItemRepository.findOrderItemsByOrderIdAndVendorId(orderId, vendorId);
	}

	@Override
	@Transactional
	public Order saveOrder(Order order) {
		return orderRepository.save(order);
	}

	@Override
	@Transactional
	public void updateOrderStatus(String orderId, Integer newStatus) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		order.setStatus(newStatus);

		Integer itemStatus = OrderStatusHelper.orderStatusToItemStatus(newStatus);
		for (OrderItem item : order.getItems()) {
			item.setStatus(itemStatus);
		}

		orderRepository.save(order);
	}

	@Override
	@Transactional
	public void updateVendorItemStatus(String orderId, String orderItemId, String vendorId, Integer newItemStatus) {
		// 1️⃣ Tìm order
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

		// 2️⃣ Tìm item thuộc vendor hiện tại
		OrderItem item = order.getItems().stream()
				.filter(i -> i.getOrderItemID().equals(orderItemId) && i.getProduct() != null
						&& i.getProduct().getOwnerUser() != null
						&& i.getProduct().getOwnerUser().getUserID().equals(vendorId))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm thuộc vendor này trong đơn hàng"));

		// 3️⃣ Cập nhật trạng thái item
		item.setStatus(newItemStatus);

		// 4️⃣ Kiểm tra nếu **tất cả item** trong order đều đã hoàn thành
		boolean allDelivered = order.getItems().stream()
				.allMatch(i -> i.getStatus() == OrderStatusHelper.ITEM_STATUS_DELIVERED);

		if (allDelivered) {
			// Nếu tất cả item đều Delivered → cập nhật trạng thái order
			order.setStatus(OrderStatusHelper.STATUS_DELIVERED);
		} else {
			// Nếu chưa thì chỉ cập nhật theo item có trạng thái thấp nhất
			Integer minItemStatus = order.getItems().stream().map(OrderItem::getStatus).min(Integer::compareTo)
					.orElse(newItemStatus);

			order.setStatus(OrderStatusHelper.itemStatusToOrderStatus(minItemStatus));
		}

		orderRepository.save(order);
	}

	@Override
	@Transactional
	public void cancelOrder(String orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		order.setStatus(OrderStatusHelper.STATUS_CANCELLED);
		for (OrderItem item : order.getItems()) {
			item.setStatus(OrderStatusHelper.ITEM_STATUS_CANCELLED);
		}

		order.setUpdateAt(LocalDateTime.now());
		orderRepository.save(order);
	}

	@Override
	@Transactional
	public void cancelVendorOrderItems(String orderId, String vendorId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		boolean hasVendorItems = false;
		for (OrderItem item : order.getItems()) {
			if (item.getProduct().getOwnerUser() != null
					&& item.getProduct().getOwnerUser().getUserID().equals(vendorId)) {
				item.setStatus(OrderStatusHelper.ITEM_STATUS_CANCELLED);
				hasVendorItems = true;
			}
		}

		if (!hasVendorItems) {
			throw new RuntimeException("No items from this vendor in the order");
		}

		boolean allCancelled = order.getItems().stream()
				.allMatch(item -> item.getStatus().equals(OrderStatusHelper.ITEM_STATUS_CANCELLED));

		if (allCancelled) {
			order.setStatus(OrderStatusHelper.STATUS_CANCELLED);
		}

		orderRepository.save(order);
	}

	@Override
	@Transactional
	public void deleteOrder(String orderId) {
		orderRepository.deleteById(orderId);
	}

	@Override
	public Map<String, Object> getRevenueChartData(String vendorId, LocalDateTime start, LocalDateTime end) {
		List<Object[]> results = orderRepository.findDailyRevenueByVendorAndPeriod(vendorId, start, end);

		List<String> dates = new ArrayList<>();
		List<Double> revenues = new ArrayList<>();

		// Tạo danh sách tất cả các ngày trong khoảng thời gian
		LocalDate currentDate = start.toLocalDate();
		LocalDate endDate = end.toLocalDate();

		Map<LocalDate, Double> revenueMap = new HashMap<>();
		for (Object[] result : results) {
			LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
			BigDecimal revenue = (BigDecimal) result[1];
			revenueMap.put(date, revenue.doubleValue());
		}

		while (!currentDate.isAfter(endDate)) {
			dates.add(currentDate.format(DateTimeFormatter.ofPattern("dd/MM")));
			revenues.add(revenueMap.getOrDefault(currentDate, 0.0));
			currentDate = currentDate.plusDays(1);
		}

		Map<String, Object> chartData = new HashMap<>();
		chartData.put("dates", dates);
		chartData.put("revenues", revenues);

		return chartData;
	}

	@Override
	public Map<String, Object> getCategoryRevenueData(String vendorId, LocalDateTime start, LocalDateTime end) {
		List<Object[]> results = orderItemRepository.findRevenueByCategoryAndVendor(vendorId, start, end);

		List<String> labels = new ArrayList<>();
		List<Double> values = new ArrayList<>();

		double totalRevenue = 0.0;

		for (Object[] result : results) {
			String categoryName = (String) result[0];
			BigDecimal revenue = (BigDecimal) result[1];

			if (categoryName == null) {
				categoryName = "Không phân loại";
			}

			double revenueValue = revenue.doubleValue();
			labels.add(categoryName);
			values.add(revenueValue);
			totalRevenue += revenueValue;
		}

		// Nếu không có dữ liệu, trả về mảng rỗng
		if (labels.isEmpty()) {
			labels.add("Không có dữ liệu");
			values.add(1.0); // Giá trị mặc định để hiển thị biểu đồ
		}

		Map<String, Object> categoryData = new HashMap<>();
		categoryData.put("labels", labels);
		categoryData.put("values", values);
		categoryData.put("totalRevenue", totalRevenue);

		return categoryData;
	}

	@Override
	public Map<String, Object> getTopProductsData(String vendorId, LocalDateTime start, LocalDateTime end) {
		// Lấy top 5 sản phẩm
		Pageable topFive = PageRequest.of(0, 5);
		List<Object[]> results = orderItemRepository.findTopProductsByVendor(vendorId, start, end, topFive);

		List<String> labels = new ArrayList<>();
		List<Long> values = new ArrayList<>();

		for (Object[] result : results) {
			String productName = (String) result[0];
			Long quantity = ((Number) result[1]).longValue();

			// Rút gọn tên sản phẩm nếu quá dài
			if (productName.length() > 20) {
				productName = productName.substring(0, 17) + "...";
			}

			labels.add(productName);
			values.add(quantity);
		}

		// Nếu không có dữ liệu, thêm item mặc định
		if (labels.isEmpty()) {
			labels.add("Không có sản phẩm nào");
			values.add(0L);
		}

		Map<String, Object> topProductsData = new HashMap<>();
		topProductsData.put("labels", labels);
		topProductsData.put("values", values);

		return topProductsData;
	}

	@Override
	public Map<String, Object> getVendorOrderStatistics(String vendorId, LocalDateTime start, LocalDateTime end) {
		Map<String, Object> statistics = new HashMap<>();

		BigDecimal totalRevenue = orderItemRepository.calculateTotalRevenueByVendor(vendorId, start, end);

		Long totalOrders = orderItemRepository.countDistinctOrdersByVendor(vendorId, start, end);
		Long deliveredOrders = orderRepository.countOrdersByVendorIdAndStatus(vendorId,
				OrderStatusHelper.STATUS_DELIVERED);

		List<Object[]> ordersByStatus = orderItemRepository.countOrdersByStatus(vendorId, start, end);

		Map<String, Long> statusMap = new HashMap<>();
		for (Object[] result : ordersByStatus) {
			Integer status = (Integer) result[0];
			Long count = (Long) result[1];
			statusMap.put(status.toString(), count);
		}

		statistics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
		statistics.put("totalOrders", totalOrders != null ? totalOrders : 0L);
		statistics.put("ordersByStatus", statusMap);
		statistics.put("startDate", start);
		statistics.put("endDate", end);
		statistics.put("deliveredOrders", deliveredOrders);

		return statistics;
	}

	// 🔥 TẠO ORDER TỪ CART VÀ TỰ ĐỘNG TẠO COMMISSION
	@Override
	@Transactional
	public Order createOrderFromCart(Cart cart, Address address, String payment_method, BigDecimal finalPrice) {
		// 1. Tạo Order (như bạn có)
		Order order = Order.builder().user(cart.getUser()).address(address).totalPrice(finalPrice) // tạm thời, sẽ cập
																									// nhật lại bên dưới
				.paymentMethod(payment_method).createAt(LocalDateTime.now()).updateAt(LocalDateTime.now()).build();
		orderRepository.save(order);

		// 2. Tạo OrderItems từ Cart
		orderItemService.createItemsFromCart(order, cart);

		// 3. Tạo OrderVoucher
		orderVoucherService.createOrderVoucherFromCart(order, cart);

		// 4. TẠO COMMISSION CHO TỪNG ORDER ITEM
		List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
		for (OrderItem orderItem : orderItems) {
			try {
				commissionService.createCommissionForOrderItem(orderItem);
			} catch (Exception e) {
				// Log lỗi nhưng không làm fail transaction
				System.out.println(
						"Lỗi khi tạo commission cho OrderItem " + orderItem.getOrderItemID() + ": " + e.getMessage());
			}
		}

		// --- 3.1: Tính lại tổng đơn từ orderItem.netTotal ---
		BigDecimal recalculatedTotal = BigDecimal.ZERO;
		for (OrderItem item : orderItems) {
			BigDecimal price = item.getPrice();
			BigDecimal qty = BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity());
			recalculatedTotal = recalculatedTotal.add(price.multiply(qty));
		}
		order.setTotalPrice(recalculatedTotal.setScale(2, RoundingMode.HALF_UP));

		orderRepository.save(order);

		// 5. Xóa Cart
		cart.getItems().clear();
		cart.getCartVouchers().clear();
		cartRepository.save(cart);

		return order;
	}

	@Override
	public List<Order> findByUser(User user) {
		return orderRepository.findByUser(user);
	}

	@Override
	public BigDecimal totalPrice(Order order) {
		BigDecimal total = BigDecimal.ZERO;
		List<OrderItem> items = orderItemRepository.findByOrder(order);
		if (items.isEmpty()) {
			return total;
		}

		for (OrderItem item : items) {
			BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
			item.setItemTotal(itemTotal);

			total = total.add(itemTotal);
		}
		return total;
	}

	@Override
	public int totalQuantity(Order order) {
		int total = 0;
		List<OrderItem> items = orderItemRepository.findByOrder(order);
		if (items.isEmpty()) {
			return total;
		}

		for (OrderItem item : items) {
			total += item.getQuantity();
		}
		return total;
	}

	@Override
	public Page<Order> findByUser(User user, Pageable pageable) {
		return orderRepository.findByUser(user, pageable);
	}

	@Override
	public Page<Order> findByUserAndStatus(User user, int status, Pageable pageable) {
		return orderRepository.findByUserAndStatus(user, status, pageable);
	}

	// ================= SHIPPER ASSIGNMENT (dựa vào ITEM STATUS) =================

	@Override
	public void assignShipper(String orderId, String shipperId, String vendorId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

		// 1) Quyền vendor: order phải có item thuộc vendor hiện tại
		boolean hasVendorItems = order.getItems().stream().anyMatch(i -> i.getProduct() != null
				&& i.getProduct().getOwnerUser() != null && vendorId.equals(i.getProduct().getOwnerUser().getUserID()));
		if (!hasVendorItems) {
			throw new RuntimeException("Bạn không có quyền gán shipper cho đơn hàng này.");
		}

		// 2) Validate shipper + role SHIPPER + active theo status (status=1 là active)
		User shipper = userRepository.findById(shipperId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy shipper."));
		if (!userRepository.isUserShipper(shipperId)) {
			throw new RuntimeException("Tài khoản được chọn không phải SHIPPER.");
		}
		if (!isUserActive(shipper)) {
			throw new RuntimeException("Shipper đã bị vô hiệu hoặc không hoạt động.");
		}

		// 3) Không cho gán nếu đơn đã khoá (mọi item đều DELIVERED/CANCELLED)
		if (isOrderLockedForAssignment(order)) {
			throw new RuntimeException("Đơn đã hoàn tất/đã huỷ, không thể gán shipper.");
		}

		// 4) Gán + tracking
		order.setShipper(shipper);
		order.setAssignedAt(LocalDateTime.now());
		order.setAssignedByVendorId(vendorId);

		// 5) KHÔNG tự nhảy trạng thái item. Vendor sẽ cập nhật item qua UI của bạn.
		// (Nếu muốn tự bump, tuỳ chọn: nếu tất cả item >= PACKED thì có thể set sang
		// SHIPPED, nhưng mình để nguyên an toàn)

		// 6) Cập nhật order.status theo "mức nhỏ nhất" của item (đồng bộ nhẹ)
		Integer minItemStatus = order.getItems().stream().map(OrderItem::getStatus).filter(Objects::nonNull)
				.min(Integer::compareTo).orElse(null);
		if (minItemStatus != null) {
			order.setStatus(OrderStatusHelper.itemStatusToOrderStatus(minItemStatus));
		}

		orderRepository.save(order);
	}

	@Override
	public void unassignShipper(String orderId, String vendorId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

		// 1) Quyền vendor
		boolean hasVendorItems = order.getItems().stream().anyMatch(i -> i.getProduct() != null
				&& i.getProduct().getOwnerUser() != null && vendorId.equals(i.getProduct().getOwnerUser().getUserID()));
		if (!hasVendorItems) {
			throw new RuntimeException("Bạn không có quyền huỷ gán shipper cho đơn hàng này.");
		}

		// 2) Không cho huỷ gán khi:
		// - Đơn đã khoá (mọi item DELIVERED/CANCELLED)
		// - HOẶC có bất kỳ item đã sang SHIPPED (đang giao)
		if (isOrderLockedForAssignment(order) || hasAnyItemFromStatus(order, OrderStatusHelper.ITEM_STATUS_SHIPPED)) {
			throw new RuntimeException("Đơn đã hoàn tất/đã huỷ hoặc đã giao/đang giao, không thể huỷ gán.");
		}

		order.setShipper(null);
		order.setAssignedAt(null);
		order.setAssignedByVendorId(null);

		// 3) Đồng bộ order.status (vẫn dựa theo min item status hiện tại)
		Integer minItemStatus = order.getItems().stream().map(OrderItem::getStatus).filter(Objects::nonNull)
				.min(Integer::compareTo).orElse(null);
		if (minItemStatus != null) {
			order.setStatus(OrderStatusHelper.itemStatusToOrderStatus(minItemStatus));
		}

		orderRepository.save(order);
	}

	@Override
	public List<Order> getOrdersByShipper(String shipperId) {
		return orderRepository.findByShipper_UserIDOrderByCreateAtDesc(shipperId);
	}

	@Override
	public List<Order> getOrdersByShipperAndStatus(String shipperId, Integer status) {
		return orderRepository.findByShipper_UserIDAndStatusOrderByCreateAtDesc(shipperId, status);
	}

	@Override
	public List<Order> getUnassignedOrdersByVendor(String vendorId) {
		return orderRepository.findUnassignedOrdersByVendor(vendorId);
	}

	@Override
	public List<Order> getOrdersByVendorAndShipper(String vendorId, String shipperId) {
		return orderRepository.findOrdersByVendorAndShipper(vendorId, shipperId);
	}

	// ================= Helpers =================

	/** Active nếu status==1 (hoặc null thì coi như active tuỳ thiết kế của bạn) */
	private boolean isUserActive(User u) {
		Integer s = (u != null) ? u.getStatus() : null;
		return s == null || s == 1;
	}

	/**
	 * Đơn coi như "khoá" cho gán/huỷ khi TẤT CẢ item đều DELIVERED hoặc CANCELLED
	 */
	private boolean isOrderLockedForAssignment(Order order) {
		if (order.getItems() == null || order.getItems().isEmpty())
			return false;

		return order.getItems().stream()
				.allMatch(i -> i.getStatus() != null && (i.getStatus().equals(OrderStatusHelper.ITEM_STATUS_DELIVERED)
						|| i.getStatus().equals(OrderStatusHelper.ITEM_STATUS_CANCELLED)));
	}

	/** Có bất kỳ item đạt/qua một mốc status nào đó (ví dụ SHIPPED) */
	private boolean hasAnyItemFromStatus(Order order, int thresholdStatus) {
		return order.getItems() != null
				&& order.getItems().stream().anyMatch(i -> i.getStatus() != null && i.getStatus() >= thresholdStatus);
	}
}