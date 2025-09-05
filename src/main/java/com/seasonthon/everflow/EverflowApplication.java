package com.seasonthon.everflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EverflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(EverflowApplication.class, args);
    }

}
