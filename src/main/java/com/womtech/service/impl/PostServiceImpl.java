package com.womtech.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.womtech.entity.Post;
import com.womtech.repository.PostRepository;
import com.womtech.service.PostService;

@Service
@Transactional
public class PostServiceImpl extends BaseServiceImpl<Post, String> implements PostService {

    private final PostRepository postRepository;

    public PostServiceImpl(JpaRepository<Post, String> repo, PostRepository postRepository) {
        super(repo);
        this.postRepository = postRepository;
    }

    @Override
    public List<Post> getAllActiveList() {
        return postRepository.findAllActiveList();
    }
    
    @Override
    public Post create(Post post) {
        if (post.getTitle() == null || post.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tiêu đề bài viết không được để trống");
        }
        if (post.getStatus() == null) post.setStatus(1); // mặc định active
        return postRepository.save(post);
    }

    @Override
    public Post update(Post post) {
        Post existing = postRepository.findById(post.getPostID())
                .orElseThrow(() -> new IllegalArgumentException("Bài viết không tồn tại"));

        existing.setTitle(post.getTitle());
        existing.setType(post.getType());
        existing.setContent(post.getContent());
        existing.setThumbnail(post.getThumbnail());
        existing.setStatus(post.getStatus());

        return postRepository.save(existing);
    }

    @Override
    public void delete(String postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Bài viết không tồn tại");
        }
        postRepository.deleteById(postId);
    }

    @Override
    public Page<Post> search(String userId, String role, String title, Integer status, Pageable pageable) {
        return postRepository.searchPosts(userId, role, title, status, pageable);
    }

    @Override
    public Page<Post> getAllActive(Pageable pageable) {
        return postRepository.findAllActive(pageable);
    }
}
