package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.money.Money;

import java.util.Objects;

/**
 * Politique de plafond sans limite.
 * <p>
 * Cette implémentation n'impose aucun plafond : tout dépôt est accepté.
 * Les invariants (montants positifs, non-nulls, etc.) sont garantis par l'agrégat {@link fr.exalt.bankaccount.domain.model.account.Account}.
 * </p>
 *
 * <h3>Pourquoi cette classe existe :</h3>
 * <ul>
 *   <li>Pour rendre explicite, dans le modèle, le fait qu’un compte (ex: CURRENT)
 *       <strong>ne possède pas de plafond</strong>.</li>
 *   <li>Elle permet d’utiliser un <em>polymorphisme uniforme</em> sur {@link CeilingPolicy} :
 *       le code du domaine peut toujours appeler <code>ceilingPolicy.validateDeposit(...)</code>
 *       sans avoir à tester le type du compte.</li>
 *   <li>Elle formalise une “règle nulle” au même niveau conceptuel qu’une règle active
 *       ({@link FixedCeiling}), ce qui renforce la lisibilité et l’évolutivité du modèle.</li>
 * </ul>
 */
public class NoCeiling implements CeilingPolicy {

    @Override
    public void validateDeposit(Money /* unused */ balance, Money deposit) {
        Objects.requireNonNull(balance, "balance");
        Objects.requireNonNull(deposit, "deposit");
        // Aucun plafond → pas d'autre vérification
    }
}
