package com.womtech.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class OrderVoucherID {
	private String orderID;
	private String voucherID;
}
