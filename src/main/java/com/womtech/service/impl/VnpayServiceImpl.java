package com.womtech.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.config.VnpayConfig;
import com.womtech.entity.Order;
import com.womtech.repository.OrderRepository;
import com.womtech.service.VnpayService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VnpayServiceImpl implements VnpayService {

    private final VnpayConfig vnpayConfig;
    private final OrderRepository orderRepository;
    private final HttpServletRequest request;

    /** Tạo URL thanh toán GET (VNPAY Payment Page) */
    @Override
    public String createPaymentUrl(Order order) throws UnsupportedEncodingException {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpayConfig.getTmnCode());

        // Amount: VND * 100
        long amount = order.getTotalPrice().longValue() * 100;
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        // TxnRef: dùng orderID bỏ dấu "-"
        String txnRef = order.getOrderID().replace("-", "");
        vnp_Params.put("vnp_TxnRef", txnRef);

        // OrderInfo
        String orderInfo = "ThanhToan_" + txnRef;
        vnp_Params.put("vnp_OrderInfo", orderInfo);

        // Return URL, locale, type
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_OrderType", "other");

        // Thời gian tạo và hết hạn
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        // IP khách hàng
        String ipAddr = request.getRemoteAddr();
        if (ipAddr.equals("0:0:0:0:0:0:0:1")) ipAddr = "127.0.0.1";
        vnp_Params.put("vnp_IpAddr", ipAddr);

        // Sắp xếp params theo tên key
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String value = vnp_Params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                // hashData dùng giá trị raw, không encode
                String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII.toString());
                hashData.append(fieldName).append('=').append(encodedValue);

                // query string encode
                encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                     .append('=').append(encodedValue);

                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        return vnpayConfig.getPayUrl() + "?" + query.toString();
    }

    /** HMAC SHA512 helper */
    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacData) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error while generating HMACSHA512", ex);
        }
    }

    @Override
    @Transactional
    public boolean handleReturn(Map<String, String> params) {
        try {

            String vnpTxnRef = params.get("vnp_TxnRef");
            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpSecureHash = params.get("vnp_SecureHash");

            // Xác thực chữ ký
            Map<String, String> sortedParams = new TreeMap<>(params);
            sortedParams.remove("vnp_SecureHash");

            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                hashData.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
            }
            if (hashData.length() > 0) hashData.deleteCharAt(hashData.length() - 1);

            String checkHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
            System.out.println("Computed hash: " + checkHash);
            if (!checkHash.equals(vnpSecureHash)) {
                System.out.println("Hash mismatch!");
                return false;
            }

            // Chuyển txnRef sang UUID chuẩn nếu bạn dùng UUID 36 ký tự trong DB
            String fullOrderId = vnpTxnRef.substring(0,8) + "-" +
                                 vnpTxnRef.substring(8,12) + "-" +
                                 vnpTxnRef.substring(12,16) + "-" +
                                 vnpTxnRef.substring(16,20) + "-" +
                                 vnpTxnRef.substring(20);

            Optional<Order> orderOpt = orderRepository.findById(fullOrderId);
            if (orderOpt.isEmpty()) {
                System.out.println("Order not found in DB!");
                return false;
            }

            Order order = orderOpt.get();
            order.setPaymentStatus("00".equals(vnpResponseCode) ? 1 : 0);
            orderRepository.save(order);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
