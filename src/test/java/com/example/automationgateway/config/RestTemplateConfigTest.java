package com.example.automationgateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RestTemplateConfigTest {

    @Test
    void restTemplateUsesSimpleClientHttpRequestFactoryAndUtf8Json() {
        RestTemplateConfig config = new RestTemplateConfig();

        RestTemplate restTemplate = config.restTemplate();
        assertNotNull(restTemplate);

        ClientHttpRequestFactory factory = restTemplate.getRequestFactory();
        assertTrue(factory instanceof SimpleClientHttpRequestFactory);

        boolean foundJsonConverter = false;
        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof MappingJackson2HttpMessageConverter json) {
                foundJsonConverter = true;
                assertEquals(StandardCharsets.UTF_8, json.getDefaultCharset());
            }
        }
        assertTrue(foundJsonConverter, "MappingJackson2HttpMessageConverter should be present");
    }
}
