package fr.exalt.bankaccount.domain.model.account.operation;

import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.domain.model.account.AccountId;

import java.time.Instant;

public record Operation(OperationId id, AccountId accountId, Money amount, Type type, Instant at, String label) {

    /**
     * Use {@link #of(AccountId, Money, Type)} to create operations.
     * This constructor is intended for persistence only.
     */
    public Operation {

    }

    public static Operation of (AccountId accountId, Money money, Type type) {
        return null;
    }

    public Money applyTo(Money initial) {
        return null;
    }

    public enum Type { DEPOSIT, WITHDRAWAL }
}
