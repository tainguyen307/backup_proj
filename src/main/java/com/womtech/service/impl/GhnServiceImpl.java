package com.womtech.service.impl;

import com.womtech.config.GhnConfig;
import com.womtech.service.GhnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class GhnServiceImpl implements GhnService {

    @Autowired
    private GhnConfig ghnConfig;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnConfig.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private List<Map<String, Object>> fetchAndSort(String url, HttpMethod method, String body, String idKey) {
        try {
            HttpEntity<String> entity = (body != null) ? new HttpEntity<>(body, createHeaders())
                                                       : new HttpEntity<>(createHeaders());

            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            JsonNode data = mapper.readTree(response.getBody()).path("data");

            if (!data.isArray()) return Collections.emptyList();

            List<Map<String, Object>> list = new ArrayList<>();
            data.forEach(node -> list.add(mapper.convertValue(node, Map.class)));

            list.sort(Comparator.comparing(m -> String.valueOf(m.get(idKey))));
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getProvinces() {
        String url = ghnConfig.getBaseUrl() + "/master-data/province";
        return fetchAndSort(url, HttpMethod.GET, null, "ProvinceID");
    }

    @Override
    public List<Map<String, Object>> getDistricts(int provinceId) {
        String url = ghnConfig.getBaseUrl() + "/master-data/district";
        String body = "{\"province_id\":" + provinceId + "}";
        return fetchAndSort(url, HttpMethod.POST, body, "DistrictID");
    }

    @Override
    public List<Map<String, Object>> getWards(int districtId) {
        String url = ghnConfig.getBaseUrl() + "/master-data/ward";
        String body = "{\"district_id\":" + districtId + "}";
        return fetchAndSort(url, HttpMethod.POST, body, "WardCode");
    }
}