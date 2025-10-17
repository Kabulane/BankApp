package fr.exalt.bankaccount.domain.model.exception;

import fr.exalt.bankaccount.domain.model.money.Money;

public final class InsufficientFundsException extends BusinessRuleViolationException {
    public InsufficientFundsException(Money balance, Money amount, Money overdraftAllowed) {
        super("Insufficient funds: amount %s, balance %s, overdraft %s"
                .formatted(amount, balance, overdraftAllowed));
    }
}
