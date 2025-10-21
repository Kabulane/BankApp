package fr.exalt.bankaccount.application.service.operation;

import fr.exalt.bankaccount.application.port.out.OperationRepository;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;

import java.time.Clock;
import java.util.List;

public class OperationService {
    private final OperationRepository operationRepository;
    private final Clock clock;

    public OperationService(OperationRepository operationRepository, Clock clock) {
        this.operationRepository = operationRepository;
        this.clock = clock;
    }

    public List<Operation> getMonthlyOperations(AccountId accountA) {
        return null;
    }
}
