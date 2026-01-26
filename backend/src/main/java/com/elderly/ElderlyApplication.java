package com.elderly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElderlyApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElderlyApplication.class, args);
    }
}
