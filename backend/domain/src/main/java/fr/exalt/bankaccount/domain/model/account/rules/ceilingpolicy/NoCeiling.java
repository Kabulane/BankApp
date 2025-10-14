package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.Money;

public class NoCeiling implements CeilingPolicy {
    @Override
    public void validateDeposit(Money /* unused */ balance, Money deposit) {

    }
}
