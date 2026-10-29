package com.olsoftware.comerciantes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ComerciantesApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComerciantesApplication.class, args);
    }
}
