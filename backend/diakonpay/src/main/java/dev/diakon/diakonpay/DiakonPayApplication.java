package dev.diakon.diakonpay;

import dev.diakon.diakonpay.service.SMTPService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DiakonPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiakonPayApplication.class, args);
    }

    @Bean
    public CommandLineRunner testMail(SMTPService smtpService) {
        return answer -> {
            try {
                smtpService.sendMessageAboutRegistry("IT_diakon", "aqaw5ln020@mebrox.cfd");
            } catch (Exception e) {
                System.err.println("--- ОШИБКА: " + e.getMessage());
            }
        };
    }
}