package fr.exalt.bankaccount.domain.model.account.operation;

import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.domain.model.account.AccountId;

import java.time.Instant;

public record Operation(OperationId id, AccountId accountId, Money amount, Type type, Instant at, String label) {

    /**
     * Use {@link #of(AccountId, Money, Type)} to create operations.
     * This constructor is intended for persistence only.
     */
    public Operation {
        if (accountId == null) {
            throw new DomainException("Operations accountId cannot be null");
        }
        if (amount == null) {
            throw new DomainException("Operations money cannot be null");
        }
        if (type == null) {
            throw new DomainException("Operations type cannot be null");
        }
        if (at == null) {
            throw new DomainException("Operations timestamp cannot be null");
        }
        // Montant strictement positif
        if (amount.isLessThanOrEqual(Money.of("0"))) {
            throw new DomainException("Operations money value must be greater than 0.00");
        }
        // label par défaut si null
        label = (label != null) ? label : (type == Type.DEPOSIT ? "Deposit" : "Withdrawal");
    }

    public static Operation of(AccountId accountId, Money amount, Type type) {
        if (accountId == null) {
            throw new DomainException("Operations accountId cannot be null");
        }
        if (amount == null) {
            throw new DomainException("Operations money cannot be null");
        }
        if (type == null) {
            throw new DomainException("Operations type cannot be null");
        }
        OperationId id = OperationId.newId();
        Instant now = Instant.now();
        String label = (type == Type.DEPOSIT) ? "Deposit" : "Withdrawal";
        return new Operation(id, accountId, amount, type, now, label);
    }

    public Money applyTo(Money initial) {
        return switch (type) {
            case DEPOSIT -> initial.add(amount);
            case WITHDRAWAL -> initial.subtract(amount);
        };
    }



    public enum Type { DEPOSIT, WITHDRAWAL }
}
