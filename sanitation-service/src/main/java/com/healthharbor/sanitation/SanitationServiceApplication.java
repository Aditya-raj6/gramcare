package com.healthharbor.sanitation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.healthharbor.sanitation", "com.healthharbor.common"})
public class SanitationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SanitationServiceApplication.class, args);
    }
}
