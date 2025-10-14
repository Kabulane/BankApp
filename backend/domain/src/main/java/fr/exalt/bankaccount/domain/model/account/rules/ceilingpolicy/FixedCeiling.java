package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.Money;

public class FixedCeiling implements CeilingPolicy {
    private final Money ceiling;

    public FixedCeiling(Money ceiling) {
        this.ceiling = ceiling;
    }

    @Override
    public void validateDeposit(Money balance, Money of) {
    }
}
