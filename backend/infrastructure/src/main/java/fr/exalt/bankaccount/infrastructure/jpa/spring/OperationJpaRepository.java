package fr.exalt.bankaccount.infrastructure.jpa.spring;

import fr.exalt.bankaccount.infrastructure.jpa.entity.OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OperationJpaRepository extends JpaRepository<OperationEntity, UUID> {

    List<OperationEntity> findByAccountIdAndAtBetweenOrderByAtDesc(UUID accountId, Instant from, Instant to);
}
