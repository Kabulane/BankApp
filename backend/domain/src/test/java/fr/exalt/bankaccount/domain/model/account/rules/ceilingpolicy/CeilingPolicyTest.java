package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.exception.CeilingExceededException;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires des implémentations de {@link CeilingPolicy}.
 * <p>
 * Couvre les comportements des deux stratégies :
 * <ul>
 *   <li>{@link NoCeiling} : aucun plafond (tout dépôt est accepté si les invariants sont respectés)</li>
 *   <li>{@link FixedCeiling} : plafond fixe (balance + dépôt ≤ plafond)</li>
 * </ul>
 * <p>
 * Les invariants d'entrée (null, montants non positifs) sont garantis par l’agrégat Account
 * et ne sont pas testés ici. Les policies lèvent uniquement des exceptions métier spécifiques :
 * {@link CeilingExceededException}.
 * </p>
 */
@DisplayName("CeilingPolicy")
class CeilingPolicyTest {

    /**
     * Groupe de tests pour la stratégie {@link NoCeiling},
     * qui n'impose aucun plafond : tout dépôt valide est accepté.
     */
    @Nested
    @DisplayName("NoCeiling")
    class NoCeilingTest {

        private final NoCeiling policy = new NoCeiling();

        /**
         * Vérifie que {@link NoCeiling} autorise tout montant strictement positif
         * (les préconditions sont garanties par Account).
         *
         * @param amount montant du dépôt sous forme textuelle
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.01", "10.00", "1000000.00"})
        @DisplayName("autorise tout dépôt strictement positif")
        void allows_any_strictly_positive_amount(String amount) {
            assertThatCode(() -> policy.validateDeposit(Money.zero(), Money.of(amount)))
                    .doesNotThrowAnyException();
        }
    }

    /**
     * Groupe de tests pour la stratégie {@link FixedCeiling},
     * qui impose un plafond de dépôt.
     */
    @Nested
    @DisplayName("FixedCeiling")
    class FixedCeilingTest {

        private final Money CEILING = Money.of("100.00");
        private final Money BALANCE = Money.of("50.00");
        private final FixedCeiling policy = new FixedCeiling(CEILING);

        /**
         * Vérifie que {@link FixedCeiling} autorise les dépôts si la balance résultante
         * est inférieure ou égale au plafond défini.
         *
         * @param depositValue montant du dépôt autorisé
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.01", "50.00", "25.00"})
        @DisplayName("autorise ≤ plafond")
        void allows_up_to_ceiling_inclusive(String depositValue) {
            assertThatCode(() -> policy.validateDeposit(BALANCE, Money.of(depositValue)))
                    .doesNotThrowAnyException();
        }

        /**
         * Vérifie que {@link FixedCeiling} rejette tout dépôt menant à balance + dépôt > plafond.
         *
         * @param depositValue montant du dépôt supérieur au plafond
         */
        @ParameterizedTest
        @ValueSource(strings = {"100.01", "150.00", "9999.99"})
        @DisplayName("refuse > plafond")
        void rejects_above_ceiling(String depositValue) {
            assertThatThrownBy(() -> policy.validateDeposit(BALANCE, Money.of(depositValue)))
                    .isInstanceOf(CeilingExceededException.class)
                    .hasMessage("Deposit Money[value=" + depositValue + "] would exceed ceiling Money[value=100.00] (balance Money[value=50.00])");
        }
    }
}
