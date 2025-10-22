package fr.exalt.bankaccount.infrastructure;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "fr.exalt.bankaccount.infrastructure" // adapters, mappers, etc.
})
@EnableJpaRepositories(basePackages = "fr.exalt.bankaccount.infrastructure.jpa.spring")
@EntityScan(basePackages = "fr.exalt.bankaccount.infrastructure.jpa.entity")
@Import(TestJpaConfig.class)
public class TestBootConfig {}