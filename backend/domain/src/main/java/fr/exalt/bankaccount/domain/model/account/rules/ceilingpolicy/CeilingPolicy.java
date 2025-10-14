package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.Money;

public interface CeilingPolicy {
    void validateDeposit(Money balance, Money deposit);
}
