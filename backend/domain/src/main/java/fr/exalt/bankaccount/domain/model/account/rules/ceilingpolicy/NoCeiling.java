package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.domain.model.exception.DomainException;

/**
 * Politique de plafond sans limite.
 * <p>
 * Cette implémentation n'impose aucun plafond : tout dépôt est accepté.
 * Le paramètre {@code balance} est volontairement ignoré.
 * </p>
 */
public class NoCeiling implements CeilingPolicy {

    // TODO: déléguer la validation de null et montants négatifs à Account
    // pour respecter une séparation plus claire des invariants métier
    @Override
    public void validateDeposit(Money /* unused */ balance, Money deposit) {
        if (deposit == null) {
            throw new DomainException("Deposit cannot be null");
        }
        if (deposit.isLessThanOrEqual(Money.zero())) {
            throw new DomainException("Deposit must be positive and greater than 0.00");
        }
        // Aucun plafond → pas d'autre vérification
    }
}
