package fr.exalt.bankaccount.domain.model.exception;

import fr.exalt.bankaccount.domain.model.money.Money;

public final class CeilingExceededException extends BusinessRuleViolationException {
    public CeilingExceededException(Money balance, Money deposit, Money ceiling) {
        super("Deposit %s would exceed ceiling %s (balance %s)".formatted(deposit, ceiling, balance));
    }
}
