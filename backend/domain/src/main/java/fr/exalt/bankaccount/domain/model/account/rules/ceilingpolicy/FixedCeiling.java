package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.domain.model.exception.DomainException;

/**
 * Politique de plafond limite.
 * <p>
 * Cette implémentation impose un plafond : tout dépôt menant à une balance supérieure au plafond est refusé.
 * </p>
 */
public class FixedCeiling implements CeilingPolicy {
    private final Money ceiling;

    public FixedCeiling(Money ceiling) {
        this.ceiling = ceiling;
    }

    // TODO: déléguer la validation de null et montants négatifs à Account
    // pour respecter une séparation plus claire des invariants métier
    @Override
    public void validateDeposit(Money balance, Money deposit) {
        if (balance == null) {
            throw new DomainException("Balance cannot be null");
        }
        if (deposit == null) {
            throw new DomainException("Deposit cannot be null");
        }
        if (deposit.isLessThanOrEqual(Money.zero())) {
            throw new DomainException("Deposit must be positive and greater than 0.00");
        }
        // On vérifie que la somme du deposit et de la balance actuelle ne dépasse pas le plafond
        if (!(deposit.add(balance).isLessThanOrEqual(ceiling))) {
            throw new DomainException("Deposit exceeds ceiling");
        }
    }

    public Money getCeiling() {
        return ceiling;
    }
}
