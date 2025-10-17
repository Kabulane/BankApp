package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.exception.CeilingExceededException;
import fr.exalt.bankaccount.domain.model.money.Money;

import java.util.Objects;

/**
 * Politique de plafond fixe.
 * Ne lève que des exceptions métier (dépassement de plafond).
 * Les invariants (null, positifs, etc.) sont garantis par l'agrégat Account.
 */
public class FixedCeiling implements CeilingPolicy {
    private final Money ceiling;

    public FixedCeiling(Money ceiling) {
        this.ceiling = Objects.requireNonNull(ceiling, "ceiling");
    }

    @Override
    public void validateDeposit(Money balance, Money deposit) {
        Objects.requireNonNull(balance, "balance");
        Objects.requireNonNull(deposit, "deposit");

        // Seule règle métier propre à la policy : ne pas dépasser le plafond
        if (!deposit.add(balance).isLessThanOrEqual(ceiling)) {
            throw new CeilingExceededException(balance, deposit, ceiling);
        }
    }

    public Money getCeiling() {
        return ceiling;
    }
}
