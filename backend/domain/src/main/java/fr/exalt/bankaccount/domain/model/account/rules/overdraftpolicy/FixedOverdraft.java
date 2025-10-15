package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.money.Money;

public class FixedOverdraft implements OverdraftPolicy {
    private static Money overdraftLimit;

    public FixedOverdraft(Money overdraft) {
        overdraftLimit = overdraft;
    }

    @Override
    public void validateWithdraw(Money balance, Money withdraw) {
        if (balance == null) {
            throw new DomainException("Balance cannot be null");
        }
        if (withdraw == null) {
            throw new DomainException("Withdraw cannot be null");
        }
        if (withdraw.isLessThanOrEqual(Money.zero())) {
            throw new DomainException("Withdraw amount must be greater than 0.00");
        }
        if (balance.subtract(withdraw).isLessThan(overdraftLimit)) {
            throw new DomainException("Withdraw would exceeds overdraft limit");
        }
    }
}
