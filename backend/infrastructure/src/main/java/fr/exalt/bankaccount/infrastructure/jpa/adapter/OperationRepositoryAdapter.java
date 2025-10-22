package fr.exalt.bankaccount.infrastructure.jpa.adapter;

import fr.exalt.bankaccount.application.port.out.OperationRepository;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.infrastructure.jpa.mapper.OperationMapper;
import fr.exalt.bankaccount.infrastructure.jpa.spring.OperationJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class OperationRepositoryAdapter implements OperationRepository {
    private final OperationJpaRepository jpa;
    private final OperationMapper mapper = new OperationMapper();

    public OperationRepositoryAdapter (OperationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Operation save(Operation operation) {
        jpa.save(mapper.toEntity(operation));
        return operation;
    }

    @Override
    public List<Operation> findByAccountIdBetween(AccountId accountId, Instant from, Instant to) {
        return jpa.findByAccountIdAndAtBetweenOrderByAtDesc(accountId.value(), from, to)
                .stream().map(mapper::toDomain).toList();
    }
}
