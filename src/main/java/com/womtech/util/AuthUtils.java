package com.womtech.util;

import java.security.Principal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.womtech.entity.User;
import com.womtech.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserService userService;

    /**
     * Lấy user hiện tại từ Principal.
     * @param principal Principal từ controller
     * @return Optional<User> nếu có đăng nhập
     */
    public Optional<User> getCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return Optional.empty();
        }
        return userService.findById(principal.getName());
    }

    /**
     * Kiểm tra xem user đã đăng nhập chưa.
     */
    public boolean isLoggedIn(Principal principal) {
        return getCurrentUser(principal).isPresent();
    }
}
