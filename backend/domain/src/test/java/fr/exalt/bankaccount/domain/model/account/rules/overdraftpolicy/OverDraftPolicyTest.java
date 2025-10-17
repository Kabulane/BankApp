package fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy;

import fr.exalt.bankaccount.domain.model.exception.InsufficientFundsException;
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
 *   <li>{@link NoOverdraft} : aucun découvert (balance finale doit rester ≥ 0)</li>
 *   <li>{@link FixedOverdraft} : découvert fixe (balance finale doit rester ≥ limite de découvert)</li>
 * </ul>
 * <p>
 * Les invariants d'entrée (null, montants non positifs) sont garantis par l'agrégat Account
 * et ne sont pas testés ici. Les policies lèvent uniquement des exceptions métier
 * spécifiques : {@link InsufficientFundsException}.
 * </p>
 */
@DisplayName("OverdraftPolicy")
public class OverDraftPolicyTest {

    /**
     * Groupe de tests pour la stratégie {@link NoOverdraft},
     * qui interdit tout découvert : la balance après retrait doit rester ≥ 0.00.
     */
    @Nested
    @DisplayName("NoOverdraft")
    class NoOverdraftTest {

        private final NoOverdraft policy = new NoOverdraft();

        /**
         * Vérifie que {@link NoOverdraft} autorise tout retrait strictement positif tant
         * que le résultat balance - withdraw est ≥ 0.00.
         *
         * @param amount montant du retrait sous forme textuelle
         */
        @ParameterizedTest
        @ValueSource(strings = {"0.01", "10.00", "1000000.00"})
        @DisplayName("autorise tout retrait si balance finale ≥ 0")
        void allows_any_strictly_positive_amount(String amount) {
            assertThatCode(() -> policy.validateWithdraw(Money.of("1000000"), Money.of(amount)))
                    .doesNotThrowAnyException();
        }

        /**
         * Vérifie que {@link NoOverdraft} rejette les retraits donnant une balance finale < 0.00.
         */
        @ParameterizedTest
        @ValueSource(strings = {"10.05", "15.01"})
        @DisplayName("refuse balance finale négative")
        void rejects_negative_result_balance(String amount) {
            Money balance = Money.of("10");

            assertThatThrownBy(() -> policy.validateWithdraw(balance, Money.of(amount)))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessage("Insufficient funds: amount Money[value=" + amount + "], balance Money[value=10.00], overdraft Money[value=0.00]");
        }
    }

    /**
     * Groupe de tests pour la stratégie {@link FixedOverdraft},
     * qui impose un découvert autorisé (balance finale doit rester ≥ limite).
     */
    @Nested
    @DisplayName("FixedOverdraft")
    class FixedOverdraftTest {

        private final Money OVERDRAFT = Money.of("-100.00");
        private final Money BALANCE = Money.zero();
        private final FixedOverdraft policy = new FixedOverdraft(OVERDRAFT);

        /**
         * Vérifie que {@link FixedOverdraft} autorise les retraits si la balance résultante
         * est ≥ au découvert autorisé (et que le retrait est strictement positif).
         */
        @Test
        @DisplayName("autorise balance finale ≥ découvert et withdraw > 0")
        void allows_up_to_overdraft_inclusive() {
            Money m1 = Money.of("0.01");
            Money balance = Money.of("-10");

            assertThatCode(() -> policy.validateWithdraw(balance, m1))
                    .doesNotThrowAnyException();
        }

        /**
         * Vérifie que {@link FixedOverdraft} rejette les retraits menant à une balance
         * inférieure au découvert autorisé.
         *
         * @param withDrawValue montant du retrait test
         */
        @ParameterizedTest
        @ValueSource(strings = {"100.01", "150.00", "10043423.1132"})
        @DisplayName("refuse balance_finale < découvert")
        void rejects_above_overdraft(String withDrawValue) {
            assertThatThrownBy(() -> policy.validateWithdraw(BALANCE, Money.of(withDrawValue)))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessage("Insufficient funds: amount Money[value=" + Money.of(withDrawValue).value() + "], balance Money[value=0.00], overdraft Money[value=-100.00]");
        }
    }
}
