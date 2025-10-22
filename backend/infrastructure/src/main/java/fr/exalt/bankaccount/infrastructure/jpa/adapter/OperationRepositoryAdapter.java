package fr.exalt.bankaccount.infrastructure.jpa.adapter;

import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;

import java.time.Instant;
import java.util.List;

public class OperationRepositoryAdapter {
    public List<Operation> findByAccountIdBetween(AccountId accountA, Instant from, Instant to) {
        return null;
    }
}
