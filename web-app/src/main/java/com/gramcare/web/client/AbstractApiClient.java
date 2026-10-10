package com.gramcare.web.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Common helper used by every *Client class to talk to one backend
 * microservice over REST. Every call is wrapped so a downstream failure
 * (service down, connection refused, 5xx) surfaces as a single
 * ServiceUnavailableException that page controllers can catch and turn into
 * a friendly error banner, instead of a raw exception reaching the browser.
 */
public abstract class AbstractApiClient {

    protected final RestTemplate restTemplate;
    protected final String baseUrl;
    private final String serviceName;

    protected AbstractApiClient(RestTemplate restTemplate, String baseUrl, String serviceName) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.serviceName = serviceName;
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getList(String path) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl + path, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return LenientMap.ofList(response.getBody());
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    protected Map<String, Object> getObject(String path) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + path, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            return LenientMap.of(response.getBody());
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    protected Map<String, Object> post(String path, Object body) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + path, HttpMethod.POST, new HttpEntity<>(body),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            return LenientMap.of(response.getBody());
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    protected Map<String, Object> put(String path, Object body) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + path, HttpMethod.PUT, new HttpEntity<>(body),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            return LenientMap.of(response.getBody());
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    protected long getLong(String path) {
        try {
            Long value = restTemplate.getForObject(baseUrl + path, Long.class);
            return value != null ? value : 0L;
        } catch (RestClientException ex) {
            throw translate(ex);
        }
    }

    /**
     * A 4xx/5xx from a service carries a JSON body with a "message" field
     * (see each service's GlobalExceptionHandler); show that to the user.
     * Anything else (connection refused, timeout) means the service is down.
     */
    private ServiceUnavailableException translate(RestClientException ex) {
        if (ex instanceof HttpStatusCodeException http) {
            String body = http.getResponseBodyAsString();
            String message = null;
            int idx = body.indexOf("\"message\"");
            if (idx >= 0) {
                int start = body.indexOf('"', body.indexOf(':', idx) + 1);
                int end = start >= 0 ? body.indexOf('"', start + 1) : -1;
                if (start >= 0 && end > start) message = body.substring(start + 1, end);
            }
            if (message == null || message.isBlank()) message = "request was rejected (HTTP " + http.getStatusCode().value() + ")";
            return new ServiceUnavailableException(serviceName + ": " + message);
        }
        return new ServiceUnavailableException(serviceName, ex);
    }
}
