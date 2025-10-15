package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.FixedCeiling;
import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires des implémentations de {@link OverdraftPolicy}.
 * <p>
 * Couvre les comportements des deux stratégies :
 * <ul>
 *   <li>{@link NoOverdraft} : aucun découvert, mais dépôt strictement positif requis</li>
 *   <li>{@link FixedOverdraft} : découvert fixe, la difference du retrait et de la balance actuelle
 *      du compte ne doit pas être inférieure au découvert</li>
 * </ul>
 * Les tests vérifient la conformité des règles métier, les cas limites (0, négatif, arrondis)
 * et la cohérence des exceptions levées.
 */
@DisplayName("OverdraftPolicy")
public class OverDraftPolicyTest {
    /**
     * Groupe de tests pour la stratégie {@link NoOverdraft},
     * qui autorise tout retrait dans la limite maximale (Balance >= 0).
     */
    @Nested
    @DisplayName("NoOverdraft")
    class NoOverdraftTest {

        private final NoOverdraft policy = new NoOverdraft();

        /**
         * Vérifie que {@link NoOverdraft} autorise tout montant strictement positif tant
         * que le résultat de la soustraction est supérieur ou égale à 0.00.
         * <p>
         * Ce test garantit qu’aucune exception n’est levée pour des montants valides.
         * </p>
         *
         * @param amount montant du dépôt sous forme textuelle
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.01", "10.00", "1000000.00"})
        @DisplayName("autorise tout retrait strictement positif")
        void allows_any_strictly_positive_amount(String amount) {
            assertThatCode(() -> policy.validateWithdraw(Money.of("1000000"), Money.of(amount)))
                    .doesNotThrowAnyException();
        }

        /**
         * Vérifie que {@link NoOverdraft} rejette les retraits inférieurs ou égaux à 0.00.
         * <p>
         * Le retrait de 0 ou de valeur négative doit lever une {@link DomainException}.
         * </p>
         *
         * @param amount montant invalide du dépôt
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01", "-10.00"})
        @DisplayName("refuse zéro ou négatif")
        void rejects_zero_or_negative(String amount) {
            assertThatThrownBy(() -> policy.validateWithdraw(null, Money.of(amount)))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Withdrawals must be positive and greater than 0.00");
        }

        /**
         * Vérifie que {@link NoOverdraft} rejette les retraits donnant une balance inférieure à 0.00.
         * <p>
         * Le retrait de 0 ou de valeur négative doit lever une {@link DomainException}.
         * </p>
         *
         * @param amount montant invalide du dépôt
         */
        @ParameterizedTest
        @ValueSource(strings = {"10.05", "15.01"})
        @DisplayName("refuse zéro ou négatif")
        void rejects_negative_result_balance(String amount) {
            Money balance = Money.of("10");

            assertThatThrownBy(() -> policy.validateWithdraw(balance, Money.of(amount)))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Withdrawals must be positive and greater than 0.00");
        }

        /**
         * Vérifie que {@link NoOverdraft} rejette les retraits null.
         * <p>
         * Le retrait de null lève une DomainException {@link DomainException}.
         * </p>
         *
         */
        @Test
        @DisplayName("refuse null withdraw")
        void rejects_null_withdraw() {
            assertThatThrownBy(() -> policy.validateWithdraw(Money.of("10"), null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Withdrawal cannot be null");
        }

        /**
         * Vérifie que {@link NoOverdraft} rejette les balance null.
         * <p>
         * La balance null lève une DomainException {@link DomainException}.
         * </p>
         *
         */
        @Test
        @DisplayName("refuse null balance")
        void rejects_null_balance() {
            assertThatThrownBy(() -> policy.validateWithdraw(null, Money.of("10")))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Balance cannot be null");
        }
    }

    /**
     * Groupe de tests pour la stratégie {@link FixedOverdraft},
     * qui impose un plafond de retrait dans la limite du découvert autorisé.
     */
    @Nested
    @DisplayName("FixedOverdraft")
    class FixedOverdraftTest {

        private final Money OVERDRAFT = Money.of("-100.00");
        private final Money BALANCE = Money.zero();
        private final FixedOverdraft policy = new FixedOverdraft(OVERDRAFT);

        /**
         * Vérifie que {@link FixedOverdraft} autorise les retraits si la balance résultante
         * est supérieure ou égale au découvert autorisé, tant qu’ils sont strictement positifs.
         *
         */
        @Test
        @DisplayName("autorise balance finale >= découvert et withdraw > 0")
        void allows_up_to_overdraft_inclusive() {
            Money m1 = Money.of("0.01");
            Money balance = Money.of("-10");

            assertThatCode(() -> policy.validateWithdraw(balance, m1))
                    .doesNotThrowAnyException();
        }

        /**
         * Vérifie que {@link FixedOverdraft} rejette tout retraits dont le résultat impliquerait
         * une balance inférieure au découvert autorisé.
         * <p>
         * Une {@link DomainException} doit être levée avec un message explicite.
         * </p>
         *
         * @param withDrawValue montant du dépôt supérieur au plafond
         */
        @ParameterizedTest
        @ValueSource(strings = {"100.01", "150.00", "10043423.1132"})
        @DisplayName("refuse balance_finale < découvert")
        void rejects_above_overdraft(String withDrawValue) {
            assertThatThrownBy(() -> policy.validateWithdraw(BALANCE, Money.of(withDrawValue)))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Withdraw exceeds overdraft");
        }

        /**
         * Vérifie que {@link FixedOverdraft} rejette les retraits inférieurs ou égaux à 0.00.
         * Indépendamment de la valeur du découvert autorisé.
         * <p>
         * Le retrait de 0 ou de valeur négative doit lever une {@link DomainException}.
         * </p>
         *
         * @param withdrawValue montant invalide du dépôt
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.00", "-0.01"})
        @DisplayName("refuse zéro/négatif")
        void rejects_zero_or_negative(String withdrawValue) {
            assertThatThrownBy(() -> policy.validateWithdraw(BALANCE, Money.of(withdrawValue)))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Withdraw must be positive and greater than 0.00");
        }

        /**
         * Vérifie que {@link FixedOverdraft} rejette les dépôts null.
         * <p>
         * Le dépôt de null lève une DomainException {@link DomainException}.
         * </p>
         */
        @Test
        @DisplayName("refuse null withdraw")
        void rejects_null_withdraw() {
            assertThatThrownBy(() -> policy.validateWithdraw(Money.of("100.00"), null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("withdraw cannot be null");
        }

        /**
         * Vérifie que {@link FixedOverdraft} rejette les balance null.
         * <p>
         * Une balance null lève une DomainException {@link DomainException}.
         * </p>
         */
        @Test
        @DisplayName("refuse null balance")
        void rejects_null_balance() {
            assertThatThrownBy(() -> policy.validateWithdraw(null, Money.of("100.00")))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Balance cannot be null");
        }
    }
}
