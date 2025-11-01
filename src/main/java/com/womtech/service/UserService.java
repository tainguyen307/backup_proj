package com.womtech.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.womtech.dto.request.auth.LoginRequest;
import com.womtech.dto.request.auth.RegisterRequest;
import com.womtech.dto.response.auth.LoginResponse;
import com.womtech.dto.response.auth.RegisterResponse;
import com.womtech.entity.User;

public interface UserService extends BaseService<User, String>{
	Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
    
 // Thêm mới: trả về danh sách role name (viết hoa) theo userId
    List<String> getRolesByUserId(String userId);

    // (tuỳ chọn) nếu cần dùng theo username sau này
    default List<String> getRolesByUsername(String username) {
        return findByUsername(username)
                .map(u -> getRolesByUserId(u.getUserID()))
                .orElseGet(java.util.Collections::emptyList);
    }

    List<User> getAllUsers();
    
    boolean promoteToVendor(String userId);

    Page<User> searchUsers(String keyword, String role, Integer status, Pageable pageable); // tìm kiếm + phân trang
    void lockUser(String userId);  // khoá user
    void unlockUser(String userId); // mở khoá user
    void deleteUserById(String userId);

	List<User> findByRolename(String rolename);
	
	User save(User user);
}	