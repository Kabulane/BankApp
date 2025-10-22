package fr.exalt.bankaccount.infrastructure;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneOffset;

@TestConfiguration
public class TestJpaConfig {

    @Bean
    Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
}
