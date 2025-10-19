package com.womtech.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.womtech.entity.Chat;
import com.womtech.entity.User;
import com.womtech.repository.ChatRepository;
import com.womtech.repository.UserRepository;

@Controller
public class ChatPageController {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public ChatPageController(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    // Trang chat cho USER
    @GetMapping("/user/chat")
    public String userChatPage(
            @RequestParam(required = false) String chatId,
            @RequestParam(required = false) String vendorId,
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/auth/login";
        }

        String currentUserId = principal.getName();
        model.addAttribute("currentUserId", currentUserId);

        // Nếu chưa có chatId nhưng có vendorId => tìm hoặc tạo chat
        if (vendorId != null) {
            if (chatId == null) {
                Optional<Chat> chatOpt = chatRepository.findByUser_UserIDAndSupport_UserID(currentUserId, vendorId);

                if (chatOpt.isPresent()) {
                    chatId = chatOpt.get().getChatID();
                } else {
                    User currentUser = userRepository.findById(currentUserId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    User vendor = userRepository.findById(vendorId)
                            .orElseThrow(() -> new RuntimeException("Vendor not found"));

                    Chat newChat = Chat.builder()
                            .user(currentUser)
                            .support(vendor)
                            .build();
                    chatRepository.save(newChat);
                    chatId = newChat.getChatID();
                }
            }
        }

        model.addAttribute("chatId", chatId);
        return "user/chat";
    }

    // Trang chat cho VENDOR (support)
    @GetMapping("/vendor/chat")
    public String vendorChatPage(
            @RequestParam(required = false) String chatId,
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/auth/login";
        }

        String currentVendorId = principal.getName();
        model.addAttribute("currentVendorId", currentVendorId);
        model.addAttribute("chatId", chatId); // null nếu chưa chọn chat

        return "vendor/chat";
    }
}