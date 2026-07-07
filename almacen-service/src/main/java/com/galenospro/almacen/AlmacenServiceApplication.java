package com.galenospro.almacen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AlmacenServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlmacenServiceApplication.class, args);
    }
}
