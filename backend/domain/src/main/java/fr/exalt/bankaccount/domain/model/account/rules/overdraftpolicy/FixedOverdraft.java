package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.money.Money;

public class FixedOverdraft implements OverdraftPolicy {
    public FixedOverdraft(Money overdraft) {
    }

    @Override
    public void validateWithdraw(Money balance, Money withdraw) {

    }
}
