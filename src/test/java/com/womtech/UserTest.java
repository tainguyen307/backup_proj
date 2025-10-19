package com.womtech;

import com.womtech.entity.Role;
import com.womtech.entity.User;
import com.womtech.repository.RoleRepository;
import com.womtech.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ECommerceApplication.class)
class UserTest {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void testInsertUser() {
		Role role = roleRepository.save(Role.builder().rolename("USER").description("Người dùng mặc định").build());

		User user = User.builder().role(role).username("tester").password("123456").email("tester@example.com")
				.status(1).build();

		userRepository.save(user);
		System.out.println("✅ Insert thành công UserID: " + user.getUserID());
	}
}