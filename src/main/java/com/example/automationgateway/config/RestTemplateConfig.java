package com.example.automationgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * Configuration for the shared {@link RestTemplate} used by the application.
 * <p>
 * This bean is primarily used by {@code RagDirectService} to call the external
 * Python RAG Agent service. It customizes:
 * </p>
 * <ul>
 *   <li>HTTP client: uses {@link SimpleClientHttpRequestFactory} and the classic
 *       JDK {@code HttpURLConnection} (HTTP/1.1)</li>
 *   <li>Timeouts: connect timeout (5 seconds) and read timeout (10 seconds)</li>
 *   <li>Message converters: enforces UTF-8 as the default charset for JSON
 *       via {@link MappingJackson2HttpMessageConverter}</li>
 * </ul>
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a {@link RestTemplate} configured with reasonable defaults for
     * calling the RAG backend and other HTTP services.
     * <p>
     * Customizations:
     * </p>
     * <ul>
     *   <li>Connect timeout: 5 seconds</li>
     *   <li>Read timeout: 10 seconds</li>
     *   <li>For all {@link MappingJackson2HttpMessageConverter} instances,
     *       the default charset is set to UTF-8 to avoid encoding issues
     *       when sending or receiving JSON.</li>
     * </ul>
     *
     * @return a configured {@link RestTemplate} bean managed by Spring
     */
    @Bean
    public RestTemplate restTemplate() {
        // Force classic JDK HttpURLConnection (HTTP/1.1) and configure timeouts
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(10_000);

        RestTemplate restTemplate = new RestTemplate(factory);

        // Ensure UTF-8 as the default charset for JSON payloads
        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof MappingJackson2HttpMessageConverter json) {
                json.setDefaultCharset(StandardCharsets.UTF_8);
            }
        }

        return restTemplate;
    }
}
