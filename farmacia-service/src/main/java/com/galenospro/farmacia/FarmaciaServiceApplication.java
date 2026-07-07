package com.galenospro.farmacia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FarmaciaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FarmaciaServiceApplication.class, args);
    }
}
