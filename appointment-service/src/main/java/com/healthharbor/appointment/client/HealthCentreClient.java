package com.healthharbor.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "healthcenter-service")
public interface HealthCentreClient {

    @GetMapping("/api/health-centres/{id}")
    CentreDto getCentre(@PathVariable("id") Long id);

    @GetMapping("/api/health-workers/{id}")
    WorkerDto getWorker(@PathVariable("id") Long id);

    record CentreDto(Long id, String name, String village, String status) {
    }

    record WorkerDto(Long id, String name, String role, Long centreId, String status) {
    }
}
