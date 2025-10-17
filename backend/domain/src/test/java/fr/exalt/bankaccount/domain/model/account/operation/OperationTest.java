package fr.exalt.bankaccount.domain.model.account.operation;

import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static fr.exalt.bankaccount.domain.model.account.operation.Operation.Type.DEPOSIT;
import static fr.exalt.bankaccount.domain.model.account.operation.Operation.Type.WITHDRAWAL;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Operation")
class OperationTest {

    private final AccountId accountId = AccountId.newId();
    private final OperationId operationId = OperationId.newId();
    private final Instant now = Instant.parse("2025-10-15T10:00:00Z");

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        void should_create_valid_operation() {
            Operation op = new Operation(operationId, accountId, Money.of("100"), DEPOSIT, now, "Deposit");

            assertThat(op.amount()).isEqualTo(Money.of("100"));
            assertThat(op.type()).isEqualTo(DEPOSIT);
            assertThat(op.at()).isEqualTo(now);
            assertThat(op.label()).isEqualTo("Deposit");
        }

        @Test
        void deposit_applyTo_increases_balance() {
            Money initial = Money.of("100");
            Operation op = new Operation(operationId, accountId, Money.of("30"), DEPOSIT, now, "Deposit");

            Money result = op.applyTo(initial);

            assertThat(result).isEqualTo(Money.of("130"));
        }

        @Test
        void withdraw_applyTo_decreases_balance() {
            Money initial = Money.of("100");
            Operation op = new Operation(operationId, accountId, Money.of("30"), WITHDRAWAL, now, "Withdrawal");

            Money result = op.applyTo(initial);

            assertThat(result).isEqualTo(Money.of("70"));
        }

        @ParameterizedTest(name = "amount ''{0}'' must be strictly positive")
        @ValueSource(strings = {"-30", "0"})
        void amount_must_be_strictly_positive(String invalid) {
            assertThatThrownBy(() ->
                    new Operation(operationId, accountId, Money.of(invalid), WITHDRAWAL, now, "Withdrawal"))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Operations money value must be greater than 0.00");
        }

        @Test
        void type_must_not_be_null() {
            assertThatThrownBy(() ->
                    new Operation(operationId, accountId, Money.of("10"), null, now, "Withdrawal"))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Operations type cannot be null");
        }

        @Test
        void amount_must_not_be_null() {
            assertThatThrownBy(() ->
                    new Operation(operationId, accountId, null, WITHDRAWAL, now, "Withdrawal"))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Operations money cannot be null");
        }

        @Test
        void timestamp_must_not_be_null() {
            assertThatThrownBy(() ->
                    new Operation(operationId, accountId, Money.of("10"), DEPOSIT, null, "Deposit"))
                    .isInstanceOf(DomainException.class)
                    // ⬇️ probable correction du message (ton test précédent réutilisait le message "money cannot be null")
                    .hasMessage("Operations timestamp cannot be null");
        }

        @Test
        void accountId_must_not_be_null() {
            assertThatThrownBy(() ->
                    new Operation(operationId, null, Money.of("10"), DEPOSIT, now, "Deposit"))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Operations accountId cannot be null");
        }
    }

    @Nested
    @DisplayName("factory 'of'")
    class FactoryTests {

        @Test
        void should_generate_id_and_timestamp_and_default_label() {
            Operation op = Operation.of(accountId, Money.of("50"), WITHDRAWAL);

            assertThat(op.id()).isNotNull();
            assertThat(op.at()).isNotNull();
            assertThat(op.label()).isEqualTo("Withdrawal");
            assertThat(op.accountId()).isEqualTo(accountId);
            assertThat(op.amount()).isEqualTo(Money.of("50"));
            assertThat(op.type()).isEqualTo(WITHDRAWAL);
        }

        @Test
        void should_fail_if_amount_is_negative_or_zero() {
            assertThatThrownBy(() -> Operation.of(accountId, Money.of("-10"), DEPOSIT))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Operations money value must be greater than 0.00");

            assertThatThrownBy(() -> Operation.of(accountId, Money.of("0"), DEPOSIT))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Operations money value must be greater than 0.00");
        }

        @Test
        void should_fail_if_type_or_accountId_is_null() {
            assertThatThrownBy(() -> Operation.of(null, Money.of("10"), DEPOSIT))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Operations accountId cannot be null");

            assertThatThrownBy(() -> Operation.of(accountId, Money.of("10"), null))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Operations type cannot be null");
        }
    }
}
