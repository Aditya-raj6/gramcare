package com.healthharbor.water;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.healthharbor.water", "com.healthharbor.common"})
public class WaterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WaterServiceApplication.class, args);
    }
}
