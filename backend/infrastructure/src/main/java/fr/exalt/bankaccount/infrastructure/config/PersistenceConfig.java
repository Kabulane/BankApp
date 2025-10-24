package fr.exalt.bankaccount.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration de la persistence JPA.
 *
 * Spring Boot auto-configure EntityManagerFactory et TransactionManager.
 * Cette classe spécifie uniquement les packages à scanner.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "fr.exalt.bankaccount.infrastructure.adapter.out.persistence.repository"
)
@EntityScan(
        basePackages = "fr.exalt.bankaccount.infrastructure.adapter.out.persistence.entity"
)
@EnableTransactionManagement
public class PersistenceConfig {

}