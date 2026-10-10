package com.gramcare.inventory.service;

import com.gramcare.inventory.dto.ActionItemRequest;
import com.gramcare.inventory.dto.ActionItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Thin REST client to the Action Tracking Service. Every call here is best
 * effort: if the Action Tracking Service is unreachable or returns an error,
 * the caller still keeps its own record (the complaint/observation itself),
 * but must surface that the linked action item was NOT created, rather than
 * reporting success. See GlobalExceptionHandler and the controllers that use
 * this client for how that is surfaced.
 */
@Component
public class ActionServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ActionServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ActionServiceClient(RestTemplate restTemplate,
                                @Value("${action-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Optional<ActionItemResponse> createActionItem(ActionItemRequest request) {
        try {
            ActionItemResponse response = restTemplate.postForObject(
                    baseUrl + "/api/actions", request, ActionItemResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientException ex) {
            log.warn("Could not create linked action item in action-service ({}): {}",
                    request.getSourceReferenceType(), ex.getMessage());
            return Optional.empty();
        }
    }
}
