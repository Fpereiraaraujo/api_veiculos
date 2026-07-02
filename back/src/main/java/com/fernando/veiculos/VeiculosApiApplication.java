package com.fernando.veiculos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VeiculosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeiculosApiApplication.class, args);
    }
}
