package com.womtech.service;

import java.io.UnsupportedEncodingException;

import com.womtech.entity.Order;

public interface VnpayService {
	String createPaymentUrl(Order order) throws UnsupportedEncodingException;
	boolean handleReturn(java.util.Map<String, String> params);
}
