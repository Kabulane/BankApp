package fr.exalt.bankaccount.domain.model.event;

import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.operation.OperationId;
import fr.exalt.bankaccount.domain.model.money.Money;

import java.time.Instant;
import java.time.LocalDateTime;

public record AccountOperationPerformedEvent(
        AccountId accountId,
        OperationId operationId,
        Money amount,
        Operation.Type type,
        Instant at
) {
    public static AccountOperationPerformedEvent from(Operation operation) {
        return new AccountOperationPerformedEvent(
                operation.accountId(),
                operation.id(),
                operation.amount(),
                operation.type(),
                operation.at()
        );
    }
}
