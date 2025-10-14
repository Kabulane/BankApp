package fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy;

import fr.exalt.bankaccount.domain.model.Money;
import fr.exalt.bankaccount.domain.model.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires des implémentations de {@link CeilingPolicy}.
 * <p>
 * Couvre les comportements des deux stratégies :
 * <ul>
 *   <li>{@link NoCeiling} : aucun plafond, mais dépôt strictement positif requis</li>
 *   <li>{@link FixedCeiling} : plafond fixe, la somme du dépot et de la balance actuelle
 *      du compte ne doit pas être supérieure au plafond</li>
 * </ul>
 * Les tests vérifient la conformité des règles métier, les cas limites (0, négatif, arrondis)
 * et la cohérence des exceptions levées.
 */
@DisplayName("CeilingPolicy")
class CeilingPolicyTest {

    /**
     * Groupe de tests pour la stratégie {@link NoCeiling},
     * qui autorise tout dépôt strictement positif sans limite maximale.
     */
    @Nested
    @DisplayName("NoCeiling")
    class NoCeilingTest {

        private final NoCeiling policy = new NoCeiling();

        /**
         * Vérifie que {@link NoCeiling} autorise tout montant strictement positif.
         * <p>
         * Ce test garantit qu’aucune exception n’est levée pour des montants valides.
         * </p>
         *
         * @param amount montant du dépôt sous forme textuelle
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.01", "10.00", "1000000.00"})
        @DisplayName("autorise tout dépôt strictement positif")
        void allows_any_strictly_positive_amount(String amount) {
            // Act & Assert (pas d'exception attendue)
            assertThatCode(() -> policy.validateDeposit(null, Money.of(amount)))
                    .doesNotThrowAnyException();
        }

        /**
         * Vérifie que {@link NoCeiling} rejette les dépôts inférieurs ou égaux à 0.00.
         * <p>
         * Le dépôt de 0 ou de valeur négative doit lever une {@link DomainException}.
         * </p>
         *
         * @param amount montant invalide du dépôt
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-10.00"})
        @DisplayName("refuse zéro ou négatif")
        void rejects_zero_or_negative(String amount) {
            assertThatThrownBy(() -> policy.validateDeposit(null, Money.of(amount)))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Deposit must be positive and greater than 0.00");
        }

        /**
         * Vérifie que {@link NoCeiling} rejette les dépôts null.
         * <p>
         * Le dépôt de null lève une DomainException {@link DomainException}.
         * </p>
         *
         */
        @Test
        @DisplayName("refuse null")
        void rejects_null_deposit() {
            assertThatThrownBy(() -> policy.validateDeposit(null, null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Deposit cannot be null");
        }
    }

    /**
     * Groupe de tests pour la stratégie {@link FixedCeiling},
     * qui impose un plafond de dépôt dans la limite du plafond du compte.
     */
    @Nested
    @DisplayName("FixedCeiling")
    class FixedCeilingTest {

        private final Money CEILING = Money.of("100.00");
        private final Money BALANCE = Money.of("50.00");
        private final FixedCeiling policy = new FixedCeiling(CEILING);

        /**
         * Vérifie que {@link FixedCeiling} autorise les dépots si la balance résultante
         * est inférieurs ou égale au plafond défini, tant qu’ils sont strictement positifs.
         *
         * @param depositValue montant du dépôt autorisé
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.01", "50.00", "25.00"})
        @DisplayName("autorise ≤ plafond et > 0")
        void allows_up_to_ceiling_inclusive(String depositValue) {
            assertThatCode(() -> policy.validateDeposit(BALANCE, Money.of(depositValue)))
                    .doesNotThrowAnyException();
        }

        /**
         * Vérifie que {@link FixedCeiling} rejette tout dépôt supérieur au plafond autorisé.
         * <p>
         * Une {@link DomainException} doit être levée avec un message explicite.
         * </p>
         *
         * @param depositValue montant du dépôt supérieur au plafond
         */
        @ParameterizedTest
        @ValueSource(strings = {"100.01", "150.00", "9999.99"})
        @DisplayName("refuse > plafond")
        void rejects_above_ceiling(String depositValue) {
            assertThatThrownBy(() -> policy.validateDeposit(BALANCE, Money.of(depositValue)))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Deposit exceeds ceiling");
        }

        /**
         * Vérifie que {@link FixedCeiling} rejette les dépôts inférieurs ou égaux à 0.00.
         * Indépendamment de la valeur du plafond.
         * <p>
         * Le dépôt de 0 ou de valeur négative doit lever une {@link DomainException}.
         * </p>
         *
         * @param depositValue montant invalide du dépôt
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01"})
        @DisplayName("refuse zéro/négatif même avec plafond")
        void rejects_zero_or_negative(String depositValue) {
            assertThatThrownBy(() -> policy.validateDeposit(BALANCE, Money.of(depositValue)))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Deposit must be positive and greater than 0.00");
        }

        /**
         * Vérifie que {@link FixedCeiling} rejette les dépôts null.
         * <p>
         * Le dépôt de null lève une DomainException {@link DomainException}.
         * </p>
         *
         */
        @Test
        @DisplayName("refuse null deposit")
        void rejects_null_deposit() {
            assertThatThrownBy(() -> policy.validateDeposit(Money.of("100.00"), null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Deposit cannot be null");
        }

        /**
         * Vérifie que {@link FixedCeiling} rejette les balance null.
         * <p>
         * Une balance null lève une DomainException {@link DomainException}.
         * </p>
         *
         */
        @Test
        @DisplayName("refuse null balance")
        void rejects_null_balance() {
            assertThatThrownBy(() -> policy.validateDeposit(null, Money.of("100.00")))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Balance cannot be null");
        }
    }
}