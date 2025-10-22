package fr.exalt.bankaccount.infrastructure.jpa.spring;

import fr.exalt.bankaccount.infrastructure.jpa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
}
