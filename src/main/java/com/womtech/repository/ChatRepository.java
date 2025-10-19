package com.womtech.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.womtech.entity.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {
	List<Chat> findByUser_UserIDOrderByCreateAtDesc(String userID);

    List<Chat> findBySupport_UserIDOrderByCreateAtDesc(String supportID);

    Optional<Chat> findByUser_UserIDAndSupport_UserID(String userID, String supportID);
    
    List<Chat> findByUser_UserIDOrSupport_UserID(String userId1, String userId2);
}