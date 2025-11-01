package com.womtech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@ConfigurationProperties(prefix = "vnpay")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VnpayConfig {
	private String tmnCode;
	private String hashSecret;
	private String payUrl;
	private String returnUrl;
	private String apiUrl;
}
