package com.oficina.oficinaapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OficinaappApplication {

    public static void main(String[] args) {
        SpringApplication.run(OficinaappApplication.class, args);
    }
}
