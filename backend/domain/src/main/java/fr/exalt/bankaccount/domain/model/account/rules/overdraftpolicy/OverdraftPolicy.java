package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.money.Money;

public interface OverdraftPolicy {
    void validateWithdraw(Money balance, Money overdraft);
}
