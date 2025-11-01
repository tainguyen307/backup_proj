package com.womtech.controller;

import com.womtech.service.GhnService;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ghn")
public class GhnController {

    private final GhnService ghnService;

    public GhnController(GhnService ghnService) {
        this.ghnService = ghnService;
    }

    @GetMapping("/provinces")
    public List<Map<String, Object>> getProvinces() {
        return ghnService.getProvinces();
    }

    @GetMapping("/districts/{provinceId}")
    public List<Map<String, Object>> getDistricts(@PathVariable int provinceId) {
        return ghnService.getDistricts(provinceId);
    }

    @GetMapping("/wards/{districtId}")
    public List<Map<String, Object>> getWards(@PathVariable int districtId) {
        return ghnService.getWards(districtId);
    }
}