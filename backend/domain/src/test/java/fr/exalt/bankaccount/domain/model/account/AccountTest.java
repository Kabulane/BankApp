package fr.exalt.bankaccount.domain.model.account;

import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.operation.OperationId;
import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.FixedCeiling;
import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.NoCeiling;
import fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy.FixedOverdraft;
import fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy.NoOverdraft;
import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static fr.exalt.bankaccount.domain.model.account.operation.Operation.Type.DEPOSIT;
import static fr.exalt.bankaccount.domain.model.account.operation.Operation.Type.WITHDRAWAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private final Clock fixedClock =
            Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneOffset.UTC);
    private final AccountId accountId = AccountId.newId();

    // -------------------------
    // Ouverture de comptes
    // -------------------------

    @Test
    void should_open_current_account_with_zero_balance_and_no_operations_and_correct_policies() {
        Account acc = Account.openCurrent(accountId, Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThat(acc.getType()).isEqualTo(Account.Type.CURRENT);
        assertThat(acc.getBalance()).isEqualTo(Money.zero());
        assertThat(acc.getOperations()).isEmpty();

        // ✅ Vérifie les TYPES concrets de policies (via extraction réflexive AssertJ)
        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(NoCeiling.class);
        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(FixedOverdraft.class);
    }

    @Test
    void should_open_savings_account_with_zero_balance_and_no_operations_and_correct_policies() {
        Account acc = Account.openSavings(accountId, Money.of("10_000"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThat(acc.getType()).isEqualTo(Account.Type.SAVINGS);
        assertThat(acc.getBalance()).isEqualTo(Money.zero());
        assertThat(acc.getOperations()).isEmpty();

        // ✅ Vérifie les TYPES concrets de policies
        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(FixedCeiling.class);
        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(NoOverdraft.class);
    }

    // -------------------------
    // Invariants de construction
    // -------------------------

    @Test
    void current_account_should_reject_positive_overdraft() {
        assertThatThrownBy(() -> Account.openCurrent(accountId, Money.of("50"), fixedClock))
                .isInstanceOf(DomainException.class)
                .hasMessage("Overdraft limit must be zero or negative.");
    }

    @Test
    void savings_account_should_require_strictly_positive_ceiling() {
        assertThatThrownBy(() -> Account.openSavings(accountId, Money.zero(), fixedClock))
                .isInstanceOf(DomainException.class)
                .hasMessage("Ceiling must be strictly positive.");

        assertThatThrownBy(() -> Account.openSavings(accountId, Money.of("-1"), fixedClock))
                .isInstanceOf(DomainException.class)
                .hasMessage("Ceiling must be strictly positive.");
    }

    // -------------------------
    // Réhydratation (pas de replay) — CURRENT & SAVINGS
    // -------------------------

    @Test
    void should_rehydrate_existing_current_account_with_correct_policies() {
        List<Operation> ops = List.of(
                Operation.of(accountId, Money.of("100"), DEPOSIT),
                Operation.of(accountId, Money.of("40"), WITHDRAWAL)
        );

        Account acc = Account.rehydrate(
                accountId,
                Account.Type.CURRENT,
                Money.of("60"),     // snapshot déjà calculé (pas de replay)
                ops,
                Money.of("-200"),   // overdraft pour CURRENT
                null,             // pas de plafond pour CURRENT
                fixedClock
        );

        Assertions.assertNotNull(acc);
        assertThat(acc.getType()).isEqualTo(Account.Type.CURRENT);
        assertThat(acc.getBalance()).isEqualTo(Money.of("60"));
        assertThat(acc.getOperations()).hasSize(2);
        assertThat(acc.getOverdraft()).isEqualTo(Money.of("-200"));

        // ✅ Policies
        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(NoCeiling.class);
        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(FixedOverdraft.class);
    }

    @Test
    void should_rehydrate_existing_savings_account_with_correct_policies() {
        List<Operation> ops = List.of(
                Operation.of(accountId, Money.of("500"), DEPOSIT),
                Operation.of(accountId, Money.of("100"), WITHDRAWAL)
        );

        Account acc = Account.rehydrate(
                accountId,
                Account.Type.SAVINGS,
                Money.of("600"),     // snapshot déjà calculé
                ops,
                null,              // pas de découvert pour SAVINGS
                Money.of("1000"),   // plafond pour SAVINGS
                fixedClock
        );

        Assertions.assertNotNull(acc);
        assertThat(acc.getType()).isEqualTo(Account.Type.SAVINGS);
        assertThat(acc.getBalance()).isEqualTo(Money.of("600"));
        assertThat(acc.getOperations()).hasSize(2);
        assertThat(acc.getCeiling()).isEqualTo(Money.of("1000"));

        // ✅ Policies
        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(FixedCeiling.class);
        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(NoOverdraft.class);
    }

    // -------------------------
    // Dépôt / Retrait - Courant
    // -------------------------

    @Test
    void current_account_deposit_should_increase_balance_and_record_operation() {
        Account acc = Account.openCurrent(accountId, Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.deposit(Money.of("250"));

        assertThat(acc.getBalance()).isEqualTo(Money.of("250"));
        assertThat(acc.getOperations())
                .hasSize(1)
                .extracting(Operation::type)
                .containsExactly(Operation.Type.DEPOSIT);
    }

    @Test
    void current_account_withdraw_should_obey_overdraft_limit() {
        Account acc = Account.openCurrent(accountId, Money.of("-100"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.withdraw(Money.of("60")); // balance = -60
        assertThat(acc.getBalance()).isEqualTo(Money.of("-60"));

        acc.withdraw(Money.of("40")); // balance = -100
        assertThat(acc.balance()).isEqualTo(Money.of("-100"));

        assertThatThrownBy(() -> acc.withdraw(Money.of("1"))) // irait à -101
                .isInstanceOf(DomainException.class)
                .hasMessage("Overdraft exceeded.");

        assertThat(acc.getOperations().stream().filter(
                o -> o.type() == WITHDRAWAL).count()
        ).isEqualTo(2);
    }

    // -------------------------
    // Dépôt / Retrait - Épargne
    // -------------------------

    @Test
    void savings_account_deposit_must_respect_ceiling() {
        Account acc = Account.openSavings(accountId, Money.of("1000"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.deposit(Money.of("900")); // balance = 900

        assertThatThrownBy(() -> acc.deposit(Money.of("200"))) // 1100 > 1000
                .isInstanceOf(DomainException.class)
                .hasMessage("Deposit would exceed account ceiling.");

        assertThat(acc.getBalance()).isEqualTo(Money.of("900"));
    }

    @Test
    void savings_account_withdraw_should_not_allow_negative_balance() {
        Account acc = Account.openSavings(accountId, Money.of("500"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.deposit(Money.of("50")); // balance = 50

        assertThatThrownBy(() -> acc.withdraw(Money.of("60")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Withdraw would make balance negative and overdraft is not allowed.");

        assertThat(acc.getBalance()).isEqualTo(Money.of("50"));
    }

    // -------------------------
    // Montants non positifs
    // -------------------------

    @Test
    void deposit_and_withdraw_should_reject_non_positive_amounts_with_exact_messages() {
        Account acc = Account.openCurrent(accountId, Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.deposit(Money.zero()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Deposit amount must be strictly positive");
        assertThatThrownBy(() -> acc.deposit(Money.of("-1")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Deposit amount must be strictly positive");

        assertThatThrownBy(() -> acc.withdraw(Money.zero()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Withdraw amount must be strictly positive");
        assertThatThrownBy(() -> acc.withdraw(Money.of("-1")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Withdraw amount must be strictly positive");

        assertThat(acc.getOperations()).isEmpty();
        assertThat(acc.getBalance()).isEqualTo(Money.zero());
    }

    // -------------------------
    // Ajustements des policies
    // -------------------------

    @Test
    @DisplayName("adjustOverdraftLimit sur CURRENT avec -50 → OK et policy = FixedOverdraft(-50)")
    void adjust_overdraft_limit_on_current_should_update_policy_and_value() {
        Account acc = Account.openCurrent(accountId, Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.adjustOverdraftLimit(Money.of("-50"));

        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(FixedOverdraft.class);
        assertThat(acc.getOverdraft()).isEqualTo(Money.of("-50"));
    }

    @Test
    @DisplayName("adjustOverdraftLimit sur SAVINGS → jette 'Only CURRENT accounts can adjust overdraft.'")
    void adjust_overdraft_limit_on_savings_should_throw() {
        Account acc = Account.openSavings(accountId, Money.of("5000"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.adjustOverdraftLimit(Money.of("-50")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Only CURRENT accounts can adjust overdraft.");
    }

    @Test
    @DisplayName("adjustCeiling sur SAVINGS avec 5000 → OK et policy = FixedCeiling(5000)")
    void adjust_ceiling_on_savings_should_update_policy_and_value() {
        Account acc = Account.openSavings(accountId, Money.of("1000"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.adjustCeiling(Money.of("5000"));

        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(FixedCeiling.class);
        assertThat(acc.getCeiling()).isEqualTo(Money.of("5000"));
    }

    @Test
    @DisplayName("adjustCeiling sur CURRENT → jette 'Only SAVINGS accounts can adjust ceiling.'")
    void adjust_ceiling_on_current_should_throw() {
        Account acc = Account.openCurrent(accountId, Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.adjustCeiling(Money.of("5000")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Only SAVINGS accounts can adjust ceiling.");
    }

    @Test
    @DisplayName("adjustOverdraftLimit : rejette une valeur > 0 avec message exact")
    void adjust_overdraft_limit_positive_should_throw_exact_message() {
        Account acc = Account.openCurrent(accountId, Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.adjustOverdraftLimit(Money.of("1")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Overdraft limit must be zero or negative.");
    }

    @Test
    @DisplayName("adjustCeiling : rejette une valeur <= 0 avec message exact")
    void adjust_ceiling_non_positive_should_throw_exact_message() {
        Account acc = Account.openSavings(accountId, Money.of("1000"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.adjustCeiling(Money.zero()))
                .isInstanceOf(DomainException.class)
                .hasMessage("Ceiling must be strictly positive.");

        assertThatThrownBy(() -> acc.adjustCeiling(Money.of("-1")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Ceiling must be strictly positive.");
    }

    // -------------------------
    // Ordre inverse chronologique des opérations (CURRENT & SAVINGS)
    // -------------------------

    @Test
    @DisplayName("addOperationToOperationList (CURRENT) : stocke en ordre 'at' décroissant (plus récente d'abord)")
    void add_operation_orders_reverse_chrono_on_current() {
        Account acc = Account.openCurrent(accountId, Money.of("0"), fixedClock);

        Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2024-01-01T11:00:00Z");
        Instant t3 = Instant.parse("2024-01-01T12:00:00Z");

        Operation op1 = new Operation(OperationId.newId(), accountId, Money.of("10"), DEPOSIT, t1, "oldest");
        Operation op2 = new Operation(OperationId.newId(), accountId, Money.of("5"), WITHDRAWAL, t2, "middle");
        Operation op3 = new Operation(OperationId.newId(), accountId, Money.of("7"), DEPOSIT, t3, "youngest");

        Assertions.assertNotNull(acc);
        // Ajout dans un ordre non trié
        acc.addOperationToOperationList(op1);
        acc.addOperationToOperationList(op3);
        acc.addOperationToOperationList(op2);

        // Vérifie ordre par 'at' décroissant : t3, t2, t1
        assertThat(acc.getOperations())
                .extracting(Operation::at)
                .containsExactly(t3, t2, t1);

        assertThat(acc.getOperations())
                .extracting(Operation::type)
                .containsExactly(DEPOSIT, WITHDRAWAL, DEPOSIT);
    }
}