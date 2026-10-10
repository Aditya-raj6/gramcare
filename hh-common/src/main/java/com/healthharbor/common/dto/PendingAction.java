package com.healthharbor.common.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A unit of outstanding work exposed by a microservice for the consolidated officer dashboard.
 *
 * @param source        owning microservice (e.g. "sanitation")
 * @param category      business category (e.g. "Maintenance Task")
 * @param refId         id of the record inside the owning service
 * @param statusUrl     gateway path accepting PATCH {status} to move the item along its workflow
 * @param nextStatuses  statuses the item can be moved to
 * @param modulePath    frontend route where the full record can be managed
 */
public record PendingAction(String source, String category, Long refId, String refNumber, String title,
                            String description, String village, String priority, String status,
                            LocalDate dueDate, LocalDateTime createdAt, String statusUrl,
                            List<String> nextStatuses, String modulePath) {
}
