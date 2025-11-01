package com.womtech.controller;

import com.womtech.entity.InventoryRequest;
import com.womtech.repository.InventoryRequestRepository;
import com.womtech.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/inventory-requests")
public class AdminInventoryRequestController {

    @Autowired
    private InventoryRequestRepository inventoryRequestRepository;

    @Autowired
    private ProductRepository productRepository;

    // 🟢 Danh sách tất cả yêu cầu
    @GetMapping
    public String listAllRequests(Model model) {
        List<InventoryRequest> requests = inventoryRequestRepository.findAll();
        model.addAttribute("requests", requests);
        return "admin/inventory-request-list";
    }

    // 🟢 Duyệt yêu cầu (chưa cộng vào kho, sẽ làm ở bước 3)
    @PostMapping("/{id}/approve")
    public String approveRequest(@PathVariable Long id) {
        InventoryRequest req = inventoryRequestRepository.findById(id).orElseThrow();
        req.setStatus(InventoryRequest.RequestStatus.APPROVED);
        inventoryRequestRepository.save(req);
        return "redirect:/admin/inventory-requests?approved";
    }

    // 🟢 Từ chối yêu cầu
    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id) {
        InventoryRequest req = inventoryRequestRepository.findById(id).orElseThrow();
        req.setStatus(InventoryRequest.RequestStatus.REJECTED);
        inventoryRequestRepository.save(req);
        return "redirect:/admin/inventory-requests?rejected";
    }
}
