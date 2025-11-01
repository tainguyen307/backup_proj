package com.womtech.controller;

import com.womtech.entity.Category;
import com.womtech.entity.Commission;
import com.womtech.entity.CommissionSetting;
import com.womtech.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/admin/commission")
@RequiredArgsConstructor
public class AdminCommissionController {

    private final CommissionService commissionService;

    // ===== QUẢN LÝ COMMISSION SETTING =====

    /**
     * Trang hiển thị form cấu hình commission
     * GET /admin/commission/setting
     */
    @GetMapping("/setting")
    public String showSettingPage(Model model) {
        // Lấy Global setting
        CommissionSetting globalSetting = commissionService.getGlobalSetting();
        model.addAttribute("globalSetting", globalSetting);
        
        // Lấy tất cả categories (với subcategories qua lazy loading)
        List<Category> categories = commissionService.getAllCategoriesWithSettings();
        model.addAttribute("categories", categories);
        
        // Service để check setting trong view
        model.addAttribute("commissionService", commissionService);
        
        return "admin/commission-setting";
    }

    /**
     * Lưu cấu hình commission (nhận trực tiếp từ form)
     * POST /admin/commission/setting/save
     */
    @PostMapping("/setting/save")
    public String saveSetting(
            @RequestParam(required = false) Double globalRate,
            @RequestParam(required = false) List<String> categoryIds,
            @RequestParam(required = false) List<Double> categoryRates,
            @RequestParam(required = false) List<String> subcategoryIds,
            @RequestParam(required = false) List<Double> subcategoryRates,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String username = authentication.getName();
            commissionService.saveAllSettings(
                globalRate, 
                categoryIds, 
                categoryRates, 
                subcategoryIds, 
                subcategoryRates, 
                username
            );
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật cấu hình chiết khấu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/commission/setting";
    }

    /**
     * Reset về mặc định
     * POST /admin/commission/setting/reset
     */
    @PostMapping("/setting/reset")
    public String resetToDefault(
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String username = authentication.getName();
            commissionService.resetToDefault(username);
            redirectAttributes.addFlashAttribute("successMessage", "Đã reset về mặc định (Global = 10%)");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/commission/setting";
    }

    // ===== BÁO CÁO COMMISSION =====

    /**
     * Trang báo cáo commission
     * GET /admin/commission/report
     */
    @GetMapping("/report")	
    public String showCommissionReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Model model
    ) {
        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1).atStartOfDay();
            endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        }

        List<Object[]> reports = commissionService.getCommissionReport(startDate, endDate);
        Double totalCommission = commissionService.getTotalCommission(startDate, endDate);

        model.addAttribute("reports", reports);
        model.addAttribute("totalCommission", totalCommission);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/commission-report";
    }

    /**
     * API: Lấy tổng commission trong khoảng thời gian (dùng cho AJAX nếu cần)
     * GET /admin/commission/api/total
     */
    @GetMapping("/api/total")
    @ResponseBody
    public Double getTotalCommission(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return commissionService.calculateTotalCommission(startDate, endDate);
    }
}