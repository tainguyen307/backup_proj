package com.womtech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;

@Configuration
@Getter
public class GhnConfig {

    @Value("${ghn.api.base}")
    private String baseUrl;

    @Value("${ghn.token}")
    private String token;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
