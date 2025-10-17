package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.exception.InsufficientFundsException;
import fr.exalt.bankaccount.domain.model.money.Money;

import java.util.Objects;

/**
 * Politique de découvert fixe.
 * <p>
 * Cette implémentation impose un découvert maximum autorisé :
 * tout retrait qui rendrait la balance inférieure à cette limite est refusé.
 * </p>
 *
 * <h3>Responsabilités :</h3>
 * <ul>
 *   <li>Vérifie qu’un retrait ne dépasse pas le découvert autorisé.</li>
 *   <li>Ne gère pas les invariants (montants positifs, null, etc.),
 *       qui sont garantis par l’agrégat {@link fr.exalt.bankaccount.domain.model.account.Account}.</li>
 * </ul>
 */
public class FixedOverdraft implements OverdraftPolicy {
    private final Money overdraftLimit;

    /**
     * @param overdraft Montant du découvert autorisé (zéro ou négatif).
     */
    public FixedOverdraft(Money overdraft) {
        overdraftLimit = overdraft;
    }

    /**
     * Vérifie que le retrait est autorisé par rapport au découvert.
     *
     * @param balance Solde actuel du compte (non null)
     * @param withdraw Montant du retrait (strictement positif)
     * @throws InsufficientFundsException si le retrait dépasse le découvert autorisé
     */
    @Override
    public void validateWithdraw(Money balance, Money withdraw) {
        Objects.requireNonNull(balance, "balance");
        Objects.requireNonNull(withdraw, "withdraw");

        // Règle métier : interdiction de dépasser le découvert autorisé
        if (balance.subtract(withdraw).isLessThan(overdraftLimit)) {
            throw new InsufficientFundsException(balance, withdraw, overdraftLimit);
        }
    }

    public Money getOverdraft() {
        return overdraftLimit;
    }
}
