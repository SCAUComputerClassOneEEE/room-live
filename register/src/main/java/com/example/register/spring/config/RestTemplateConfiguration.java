package com.example.register.spring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate( ) {
        return new RestTemplate();
    }
}
