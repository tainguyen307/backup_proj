package com.womtech.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.womtech.entity.Post;

public interface PostService extends BaseService<Post, String> {
	Post create(Post post);

	Post update(Post post);

	void delete(String postId);
	
	List<Post> getAllActiveList();

	Optional<Post> findById(String postId);

	Page<Post> search(String userId, String role, String title, Integer status, Pageable pageable);

	Page<Post> getAllActive(Pageable pageable);
}