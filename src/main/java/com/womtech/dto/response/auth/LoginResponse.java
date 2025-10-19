package com.womtech.dto.response.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String userID;
    private String username;
    private String message;
    private String redirectUrl;
}