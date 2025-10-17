package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.money.Money;

/**
 * Politique de découvert non autorisé.
 * <p>
 * Cette implémentation empêche tout découvert : tout retrait résultant à une balance négative est refusé.
 * </p>
 */
public class NoOverdraft implements OverdraftPolicy {
    private static final Money overdraftLimit = Money.of("0");

    // TODO: déléguer la validation de null et montants négatifs à Account
    // pour respecter une séparation plus claire des invariants métier
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
