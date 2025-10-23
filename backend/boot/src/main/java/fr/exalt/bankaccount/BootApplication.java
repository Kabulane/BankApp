package fr.exalt.bankaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "fr.exalt.bankaccount.infrastructure.jpa")
@EnableJpaRepositories(basePackages = "fr.exalt.bankaccount.infrastructure.jpa")// ou où sont tes @Entity
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }
}
