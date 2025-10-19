package com.womtech.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

	@NotBlank(message = "Username is required")
	@Size(min = 4, max = 150, message = "Username must be between 4 and 150 characters")
	private String username;

	@NotBlank(message = "Password is required")
	@Size(min = 6, max = 255, message = "Password must be at least 6 characters")
	private String password;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Size(max = 255)
	private String email;
}