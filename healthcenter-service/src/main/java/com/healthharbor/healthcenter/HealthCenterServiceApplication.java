package com.healthharbor.healthcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.healthharbor.healthcenter", "com.healthharbor.common"})
public class HealthCenterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthCenterServiceApplication.class, args);
    }
}
