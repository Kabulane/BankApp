package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.money.Money;

public class NoOverdraft implements OverdraftPolicy {
    @Override
    public void validateWithdraw(Money balance, Money overdraft) {

    }
}
