package com.womtech.service.impl;

import com.womtech.entity.Order;
import com.womtech.entity.OrderItem;
import com.womtech.entity.User;
import com.womtech.repository.OrderItemRepository;
import com.womtech.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.womtech.entity.Address;
import com.womtech.entity.Cart;
import com.womtech.service.AddressService;
import com.womtech.service.CartItemService;
import com.womtech.service.CartService;
import com.womtech.service.OrderItemService;
import com.womtech.service.OrderService;
import com.womtech.service.VoucherService;
import com.womtech.util.OrderStatusHelper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderServiceImpl extends BaseServiceImpl<Order, String> implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
	CartService cartService;
	@Autowired
    CartItemService cartItemService;
	@Autowired
    AddressService addressService;
	@Autowired
	OrderItemService orderItemService;
	@Autowired
	VoucherService voucherService;
	
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
    public List<Order> getOrdersByVendorIdAndDateRange(String vendorId, LocalDateTime startDate, LocalDateTime endDate) {
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

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
                .filter(i -> i.getOrderItemID().equals(orderItemId)
                        && i.getProduct() != null
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
            Integer minItemStatus = order.getItems().stream()
                    .map(OrderItem::getStatus)
                    .min(Integer::compareTo)
                    .orElse(newItemStatus);

            order.setStatus(OrderStatusHelper.itemStatusToOrderStatus(minItemStatus));
        }

        orderRepository.save(order);
    }


    @Override
    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        boolean hasVendorItems = false;
        for (OrderItem item : order.getItems()) {
            if (item.getProduct().getOwnerUser() != null &&
                item.getProduct().getOwnerUser().getUserID().equals(vendorId)) {
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
        Long deliveredOrders = orderRepository.countOrdersByVendorIdAndStatus(vendorId,OrderStatusHelper.STATUS_DELIVERED);

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
    
    @Override
	public Order createOrder(User user, Address address, String payment_method, String voucherCode) {
        Cart cart = cartService.findByUser(user);
        // Chưa thêm voucher
        BigDecimal total = totalPrice(cart, voucherCode);
        
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(total)
                .paymentMethod(payment_method)
                .totalPrice(total)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        orderItemService.createItemsFromCart(order, cart);
        
        cartService.clearCart(user);

        return order;
    }
    
    @Override
	public BigDecimal totalPrice(Cart cart, String voucherCode) {
    	BigDecimal total = cartService.totalPrice(cart);
    	
//    	Optional<Voucher> voucherOpt = voucherService.findByCode(voucherCode);
//    	if (voucherOpt.isEmpty())
//    		return total;
//    	
//    	Voucher voucher = voucherOpt.get();
//    	if (voucherService.valid(voucher, total))
//    		total = voucherService.discountPrice;
    	
        return total;
    }
    
    @Override
	public BigDecimal totalPrice(Order order) {
    	BigDecimal total = BigDecimal.ZERO;
    	List<OrderItem> items = orderItemService.findByOrder(order);
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
	public List<Order> findByUser(User user) {
		return orderRepository.findByUser(user);
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
}