package fr.exalt.bankaccount.domain.model.account;

import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.operation.OperationId;
import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.FixedCeiling;
import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.NoCeiling;
import fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy.FixedOverdraft;
import fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy.NoOverdraft;
import fr.exalt.bankaccount.domain.model.exception.BusinessRuleViolationException;
import fr.exalt.bankaccount.domain.model.exception.CeilingExceededException;
import fr.exalt.bankaccount.domain.model.exception.InsufficientFundsException;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

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
    void should_open_current_account_with_zero_balance__correct_policies() {
        Account acc = Account.openCurrent(Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThat(acc.getType()).isEqualTo(Account.Type.CURRENT);
        assertThat(acc.getBalance()).isEqualTo(Money.zero());

        // ✅ Vérifie les TYPES concrets de policies (via extraction réflexive AssertJ)
        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(NoCeiling.class);
        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(FixedOverdraft.class);
    }

    @Test
    void should_open_savings_account_with_zero_balance_and_correct_policies() {
        Account acc = Account.openSavings(Money.of("10000"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThat(acc.getType()).isEqualTo(Account.Type.SAVINGS);
        assertThat(acc.getBalance()).isEqualTo(Money.zero());

        // ✅ Vérifie les TYPES concrets de policies
        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(FixedCeiling.class);
        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(NoOverdraft.class);
    }

    // -------------------------
    // Invariants de construction (validations d'entrée)
    // -------------------------

    @Test
    void current_account_should_reject_positive_overdraft() {
        assertThatThrownBy(() -> Account.openCurrent(Money.of("50"), fixedClock))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Overdraft limit must be zero or negative");
    }

    @Test
    void savings_account_should_require_strictly_positive_ceiling() {
        assertThatThrownBy(() -> Account.openSavings(Money.zero(), fixedClock))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Ceiling must be strictly positive");

        assertThatThrownBy(() -> Account.openSavings(Money.of("-1"), fixedClock))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Ceiling must be strictly positive");
    }

    // -------------------------
    // Réhydratation (pas de replay) — CURRENT & SAVINGS
    // -------------------------

    @Test
    void should_rehydrate_existing_current_account_with_correct_policies() {
        Account acc = Account.rehydrate(
                accountId,
                Account.Type.CURRENT,
                Money.of("60"),     // snapshot déjà calculé (pas de replay)
                Money.of("-200"),   // overdraft pour CURRENT
                null,               // pas de plafond pour CURRENT
                fixedClock
        );

        Assertions.assertNotNull(acc);
        assertThat(acc.getType()).isEqualTo(Account.Type.CURRENT);
        assertThat(acc.getBalance()).isEqualTo(Money.of("60"));
        assertThat(acc.getOverdraft()).isEqualTo(Money.of("-200"));

        // ✅ Policies
        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(NoCeiling.class);
        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(FixedOverdraft.class);
    }

    @Test
    void should_rehydrate_existing_savings_account_with_correct_policies() {
        Account acc = Account.rehydrate(
                accountId,
                Account.Type.SAVINGS,
                Money.of("600"),     // snapshot déjà calculé
                null,      // pas de découvert pour SAVINGS
                Money.of("1000"),    // plafond pour SAVINGS
                fixedClock
        );

        Assertions.assertNotNull(acc);
        assertThat(acc.getType()).isEqualTo(Account.Type.SAVINGS);
        assertThat(acc.getBalance()).isEqualTo(Money.of("600"));
        assertThat(acc.getCeiling()).isEqualTo(Money.of("1000"));

        // ✅ Policies
        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(FixedCeiling.class);
        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(NoOverdraft.class);
    }

    // -------------------------
    // Dépôt / Retrait - Courant
    // -------------------------

    @Test
    void current_account_deposit_should_increase_balance() {
        Account acc = Account.openCurrent(Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.deposit(Money.of("250"));

        assertThat(acc.getBalance()).isEqualTo(Money.of("250"));
    }

    @Test
    void current_account_withdraw_should_obey_overdraft_limit() {
        Account acc = Account.openCurrent(Money.of("-100"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.withdraw(Money.of("60")); // balance = -60
        assertThat(acc.getBalance()).isEqualTo(Money.of("-60"));

        acc.withdraw(Money.of("40")); // balance = -100
        assertThat(acc.balance()).isEqualTo(Money.of("-100"));

        assertThatThrownBy(() -> acc.withdraw(Money.of("1"))) // irait à -101
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds: amount Money[value=1.00], balance Money[value=-100.00], overdraft Money[value=-100.00]");
    }

    // -------------------------
    // Dépôt / Retrait - Épargne
    // -------------------------

    @Test
    void savings_account_deposit_must_respect_ceiling() {
        Account acc = Account.openSavings(Money.of("1000"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.deposit(Money.of("900")); // balance = 900

        assertThatThrownBy(() -> acc.deposit(Money.of("200"))) // 1100 > 1000
                .isInstanceOf(CeilingExceededException.class)
                .hasMessage("Deposit Money[value=200.00] would exceed ceiling Money[value=1000.00] (balance Money[value=900.00])");

        assertThat(acc.getBalance()).isEqualTo(Money.of("900"));
    }

    @Test
    void savings_account_withdraw_should_not_allow_negative_balance() {
        Account acc = Account.openSavings(Money.of("500"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.deposit(Money.of("50")); // balance = 50

        assertThatThrownBy(() -> acc.withdraw(Money.of("60")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds: amount Money[value=60.00], balance Money[value=50.00], overdraft Money[value=0.00]");

        assertThat(acc.getBalance()).isEqualTo(Money.of("50"));
    }

    // -------------------------
    // Montants non positifs
    // -------------------------

    @Test
    void deposit_and_withdraw_should_reject_non_positive_amounts_with_exact_messages() {
        Account acc = Account.openCurrent(Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.deposit(Money.zero()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Deposit amount must be strictly positive");
        assertThatThrownBy(() -> acc.deposit(Money.of("-1")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Deposit amount must be strictly positive");

        assertThatThrownBy(() -> acc.withdraw(Money.zero()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Withdraw amount must be strictly positive");
        assertThatThrownBy(() -> acc.withdraw(Money.of("-1")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Withdraw amount must be strictly positive");

        assertThat(acc.getBalance()).isEqualTo(Money.zero());
    }

    // -------------------------
    // Ajustements des policies
    // -------------------------

    @Test
    @DisplayName("adjustOverdraftLimit sur CURRENT avec -50 → OK et policy = FixedOverdraft(-50)")
    void adjust_overdraft_limit_on_current_should_update_policy_and_value() {
        Account acc = Account.openCurrent(Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.adjustOverdraftLimit(Money.of("-50"));

        assertThat(acc).extracting("overdraftPolicy").isInstanceOf(FixedOverdraft.class);
        assertThat(acc.getOverdraft()).isEqualTo(Money.of("-50"));
    }

    @Test
    @DisplayName("adjustOverdraftLimit sur SAVINGS → jette 'Only CURRENT accounts can adjust overdraft.'")
    void adjust_overdraft_limit_on_savings_should_throw() {
        Account acc = Account.openSavings(Money.of("5000"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.adjustOverdraftLimit(Money.of("-50")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Only CURRENT accounts can adjust overdraft");
    }

    @Test
    @DisplayName("adjustCeiling sur SAVINGS avec 5000 → OK et policy = FixedCeiling(5000)")
    void adjust_ceiling_on_savings_should_update_policy_and_value() {
        Account acc = Account.openSavings(Money.of("1000"), fixedClock);

        Assertions.assertNotNull(acc);
        acc.adjustCeiling(Money.of("5000"));

        assertThat(acc).extracting("ceilingPolicy").isInstanceOf(FixedCeiling.class);
        assertThat(acc.getCeiling()).isEqualTo(Money.of("5000"));
    }

    @Test
    @DisplayName("adjustCeiling sur CURRENT → jette 'Only SAVINGS accounts can adjust ceiling.'")
    void adjust_ceiling_on_current_should_throw() {
        Account acc = Account.openCurrent(Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.adjustCeiling(Money.of("5000")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Only SAVINGS accounts can adjust ceiling");
    }

    @Test
    @DisplayName("adjustOverdraftLimit : rejette une valeur > 0 avec message exact")
    void adjust_overdraft_limit_positive_should_throw_exact_message() {
        Account acc = Account.openCurrent(Money.of("0"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.adjustOverdraftLimit(Money.of("1")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Overdraft limit must be zero or negative");
    }

    @Test
    @DisplayName("adjustCeiling : rejette une valeur <= 0 avec message exact")
    void adjust_ceiling_non_positive_should_throw_exact_message() {
        Account acc = Account.openSavings(Money.of("1000"), fixedClock);

        Assertions.assertNotNull(acc);
        assertThatThrownBy(() -> acc.adjustCeiling(Money.zero()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Ceiling must be strictly positive");

        assertThatThrownBy(() -> acc.adjustCeiling(Money.of("-1")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Ceiling must be strictly positive");
    }
}
