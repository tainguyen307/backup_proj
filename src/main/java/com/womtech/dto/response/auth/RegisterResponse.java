package com.womtech.dto.response.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
	private String userID;
	private String username;
	private String email;
	private String message;
}