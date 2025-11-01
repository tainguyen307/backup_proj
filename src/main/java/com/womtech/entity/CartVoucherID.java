package com.womtech.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CartVoucherID {
	private String cartID;
	private String voucherID;
}
