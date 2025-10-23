package fr.exalt.bankaccount.application.service.operation;

import fr.exalt.bankaccount.application.port.out.OperationRepository;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Transactional
public class OperationService {
    private final OperationRepository operationRepository;
    private final Clock clock;

    public OperationService(OperationRepository operationRepository, Clock clock) {
        this.operationRepository = operationRepository;
        this.clock = clock;
    }

    public List<Operation> getMonthlyOperations(AccountId accountId) {
        Objects.requireNonNull(accountId);

        Instant now = Instant.now(clock);
        Instant fromInclusive = now.minus(30, ChronoUnit.DAYS);

        return operationRepository.findByAccountIdBetween(accountId, fromInclusive, now);
    }
}
