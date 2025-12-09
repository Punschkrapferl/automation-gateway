package com.example.automationgateway.service;

import com.example.automationgateway.dto.RagQueryRequest;
import com.example.automationgateway.dto.RagQueryResponse;
import com.example.automationgateway.dto.RagRetrievedDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagDirectServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Test
    void queryBuildsCorrectUrlAndReturnsBody() {
        String baseUrl = "http://rag-api:8000";
        RagDirectService service = new RagDirectService(restTemplate, baseUrl);

        RagRetrievedDocument doc = new RagRetrievedDocument(
                "d1", 0.9, "text", Map.of("k", "v")
        );
        RagQueryResponse body = new RagQueryResponse(
                "question", "answer", List.of(doc)
        );
        ResponseEntity<RagQueryResponse> responseEntity =
                new ResponseEntity<>(body, HttpStatus.OK);

        AtomicReference<String> capturedUrl = new AtomicReference<>();
        AtomicReference<HttpEntity<?>> capturedEntity = new AtomicReference<>();

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RagQueryResponse.class)
        )).thenAnswer(invocation -> {
            capturedUrl.set(invocation.getArgument(0, String.class));
            capturedEntity.set(invocation.getArgument(1, HttpEntity.class));
            return responseEntity;
        });

        RagQueryResponse result = service.query("question", 3);

        // URL must be baseUrl + /api/query
        assertEquals("http://rag-api:8000/api/query", capturedUrl.get());

        // Request payload and headers
        @SuppressWarnings("unchecked")
        HttpEntity<RagQueryRequest> sentEntity = (HttpEntity<RagQueryRequest>) capturedEntity.get();
        assertNotNull(sentEntity);

        RagQueryRequest sentBody = sentEntity.getBody();
        assertNotNull(sentBody);
        assertEquals("question", sentBody.getQuery());
        assertEquals(3, sentBody.getTopK());

        HttpHeaders headers = sentEntity.getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertTrue(headers.getAccept().contains(MediaType.APPLICATION_JSON));

        // Response mapping
        assertNotNull(result);
        assertEquals("question", result.getQuery());
        assertEquals("answer", result.getAnswer());
        assertEquals(1, result.getDocuments().size());
    }

    @Test
    void queryNormalizesBaseUrlWithTrailingSlash() {
        String baseUrl = "http://rag-api:8000/";
        RagDirectService service = new RagDirectService(restTemplate, baseUrl);

        RagQueryResponse body = new RagQueryResponse("q", "a", List.of());
        ResponseEntity<RagQueryResponse> responseEntity =
                new ResponseEntity<>(body, HttpStatus.OK);

        AtomicReference<String> capturedUrl = new AtomicReference<>();

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RagQueryResponse.class)
        )).thenAnswer(invocation -> {
            capturedUrl.set(invocation.getArgument(0, String.class));
            return responseEntity;
        });

        service.query("q", 1);

        assertEquals("http://rag-api:8000/api/query", capturedUrl.get());
    }

    @Test
    void queryNon2xxStatusThrowsIllegalStateException() {
        String baseUrl = "http://rag-api:8000";
        RagDirectService service = new RagDirectService(restTemplate, baseUrl);

        ResponseEntity<RagQueryResponse> responseEntity =
                new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RagQueryResponse.class)
        )).thenReturn(responseEntity);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.query("q", 1)
        );

        assertTrue(ex.getMessage().contains("RAG /api/query returned"));
    }

    @Test
    void queryPropagatesRestClientException() {
        String baseUrl = "http://rag-api:8000";
        RagDirectService service = new RagDirectService(restTemplate, baseUrl);

        RestClientException cause = new RestClientException("connection error");

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(RagQueryResponse.class)
        )).thenThrow(cause);

        RestClientException ex = assertThrows(
                RestClientException.class,
                () -> service.query("q", 1)
        );

        assertSame(cause, ex);
    }
}
