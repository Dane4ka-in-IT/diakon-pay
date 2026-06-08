package dev.diakon.diakonpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DiakonPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiakonPayApplication.class, args);
    }
}