package com.healthharbor.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Generic workflow transition payload used by every PATCH /{id}/status endpoint.
 */
public record StatusUpdateRequest(@NotBlank String status, String notes, String assignedTo, String updatedBy) {
}
