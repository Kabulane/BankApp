package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.exception.InsufficientFundsException;
import fr.exalt.bankaccount.domain.model.money.Money;

import java.util.Objects;

/**
 * Politique de découvert non autorisé.
 * <p>
 * Cette implémentation interdit tout découvert :
 * tout retrait qui rendrait la balance négative est refusé.
 * </p>
 *
 * <h3>Responsabilités :</h3>
 * <ul>
 *   <li>Vérifie que la balance reste ≥ 0 après un retrait.</li>
 *   <li>Ne gère pas les invariants (montants positifs, null, etc.),
 *       qui sont garantis par l’agrégat {@link fr.exalt.bankaccount.domain.model.account.Account}.</li>
 * </ul>
 *
 */
public class NoOverdraft implements OverdraftPolicy {
    /** Seuil minimal de balance autorisé (0.00). */
    private static final Money OVERDRAFT_LIMIT = Money.zero();

    @Override
    public void validateWithdraw(Money balance, Money withdraw) {
        Objects.requireNonNull(balance, "balance");
        Objects.requireNonNull(withdraw, "withdraw");

        // Règle métier : le solde après retrait ne doit pas être négatif
        if (balance.subtract(withdraw).isLessThan(OVERDRAFT_LIMIT)) {
            throw new InsufficientFundsException(balance, withdraw, OVERDRAFT_LIMIT);
        }
    }
}
