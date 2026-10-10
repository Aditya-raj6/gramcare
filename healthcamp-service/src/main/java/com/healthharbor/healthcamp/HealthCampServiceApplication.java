package com.healthharbor.healthcamp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.healthharbor.healthcamp", "com.healthharbor.common"})
public class HealthCampServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthCampServiceApplication.class, args);
    }
}
