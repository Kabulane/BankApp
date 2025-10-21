package fr.exalt.bankaccount.application.service;

import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.exception.BusinessRuleViolationException;
import fr.exalt.bankaccount.domain.model.exception.InsufficientFundsException;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class WithdrawServiceTest {

    static final class InMemoryAccountRepository implements AccountRepository {
        private final Map<AccountId, Account> store = new HashMap<>();
        @Override public Account save(Account account) { store.put(account.getId(), account); return account;}
        @Override public Account findById(AccountId id) { return store.get(id); };
    }

    @Test
    void withdraw_should_decrease_balance_and_persist() {
        //given
        AccountRepository repo = new InMemoryAccountRepository();
        Account account = Account.openCurrent(Money.of("-300"), Clock.systemUTC());
        repo.save(account);

        WithdrawService service = new WithdrawService(repo);
        WithdrawCommand cmd = new WithdrawCommand(account.getId(), Money.of("200"));

        // when
        WithdrawResult result = service.handle(cmd);

        // then
        assertThat(result.accountId()).isEqualTo(account.getId());
        assertThat(result.newBalance()).isEqualTo(Money.of("-200"));
        // Persisted state
        assertThat(repo.findById(result.accountId()).getBalance().isEqualTo(Money.of("-200"))).isTrue();
    }

    @Test
    void withdraw_negative_amount_should_propagate_business_exception() {
        AccountRepository repo = new InMemoryAccountRepository();
        Account account = Account.openCurrent(Money.of("-300"), Clock.systemUTC());
        repo.save(account);

        WithdrawService service = new WithdrawService(repo);
        WithdrawCommand cmd = new WithdrawCommand(account.getId(), Money.of("-200"));

        assertThatThrownBy(() -> service.handle(cmd)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void withdraw_exceeding_current_overdraft_should_propagate_overdraft_exception() {
        AccountRepository repo = new InMemoryAccountRepository();
        WithdrawService service = new WithdrawService(repo);
        Account account = Account.openCurrent(Money.of("-100"), Clock.systemUTC());
        repo.save(account);

        WithdrawCommand cmd = new WithdrawCommand(account.getId(), Money.of("101"));

        assertThatThrownBy(() -> service.handle(cmd)).isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void withdraw_exceeding_savings_overdraft_should_propagate_overdraft_exception() {
        AccountRepository repo = new InMemoryAccountRepository();
        WithdrawService service = new WithdrawService(repo);
        Account account = Account.openSavings(Money.of("100"), Clock.systemUTC());
        repo.save(account);

        WithdrawCommand cmd = new WithdrawCommand(account.getId(), Money.of("101"));

        assertThatThrownBy(() -> service.handle(cmd)).isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void withdraw_unknown_account_should_throw_not_found() {
        AccountRepository repo = new InMemoryAccountRepository();
        WithdrawService service = new WithdrawService(repo);
        AccountId unknownId = AccountId.newId();

        WithdrawCommand cmd = new WithdrawCommand(unknownId, Money.of("350"));

        assertThatThrownBy(() -> service.handle(cmd)).isInstanceOf(AccountNotFoundException.class);
    }
}
