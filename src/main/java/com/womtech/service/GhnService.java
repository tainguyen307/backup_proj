package com.womtech.service;

import java.util.List;
import java.util.Map;

public interface GhnService {

	List<Map<String, Object>> getProvinces();

    List<Map<String, Object>> getDistricts(int provinceId);

    List<Map<String, Object>> getWards(int districtId);
}
