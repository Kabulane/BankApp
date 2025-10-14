package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.Money;
import fr.exalt.bankaccount.domain.model.exception.DomainException;

/**
 * Politique de plafond sans limite.
 * <p>
 * Cette implémentation n'impose aucun plafond : tout dépôt est accepté.
 * Le paramètre {@code balance} est volontairement ignoré.
 * </p>
 */
public class NoCeiling implements CeilingPolicy {

    /**
     * Valide un dépôt sans appliquer de plafond.
     * <p>
     * Contrats minimalistes :
     * <ul>
     *   <li>{@code deposit} ne doit pas être null.</li>
     *   <li>{@code deposit} doit être strictement supérieur à 0.00.</li>
     *   <li>Aucun contrôle n'est fait sur {@code balance} (ignoré par conception).</li>
     * </ul>
     *
     * @param balance solde courant (ignoré)
     * @param deposit  montant du dépôt (doit être non nul)
     * @throws DomainException si {@code deposit} est nul ou non positif
     */
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
