package fr.exalt.bankaccount.application.service.account;

import fr.exalt.bankaccount.application.dto.account.operation.DepositCommand;
import fr.exalt.bankaccount.application.dto.account.operation.DepositResult;
import fr.exalt.bankaccount.application.dto.account.operation.OperationResult;
import fr.exalt.bankaccount.application.exception.AccountNotFoundApplicationException;
import fr.exalt.bankaccount.application.port.in.DepositUseCase;
import fr.exalt.bankaccount.application.port.out.AccountRepository;
import fr.exalt.bankaccount.application.port.out.OperationRepository;
import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.exception.BusinessRuleViolationException;
import fr.exalt.bankaccount.domain.model.exception.CeilingExceededException;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class DepositServiceTest {

    static final class InMemoryAccountRepository implements AccountRepository {
        private final Map<AccountId, Account> store = new HashMap<>();
        @Override public Account save(Account account) { store.put(account.getId(), account); return account;}
        @Override public Account findById(AccountId id) { return store.get(id); }
    }

    static final class InMemoryOperationRepository implements OperationRepository {
        final List<Operation> saved = new ArrayList<>();

        @Override
        public Operation save (Operation operation) {
            saved.add(operation);
            return operation;
        }

        @Override
        public List<Operation> findByAccountIdBetween(AccountId accountId, Instant from, Instant to) {
            return saved.stream()
                    .filter(operation -> operation.accountId().equals(accountId))
                    .filter(operation -> !operation.at().isBefore(from) && !operation.at().isAfter(to))
                    .sorted(Comparator.comparing(Operation::at).reversed())
                    .toList();
        }
    }

    Clock clock = Clock.fixed(Instant.parse("2025-10-21T08:00:00Z"), ZoneOffset.UTC);
    Instant before = Clock.fixed(Instant.parse("2025-09-21T08:00:00Z"), ZoneOffset.UTC).instant();
    Instant after = Clock.fixed(Instant.parse("2025-11-21T08:00:00Z"), ZoneOffset.UTC).instant();
    
    @Test
    void deposit_should_increase_balance_and_persist() {
        //given
        AccountRepository accountRepository = new InMemoryAccountRepository();
        OperationRepository operationRepository = new InMemoryOperationRepository();
        
        Account account = Account.openCurrent(Money.of("-300"), clock);
        accountRepository.save(account);

        DepositUseCase service = new DepositService(accountRepository, operationRepository);
        DepositCommand cmd = new DepositCommand(account.getId(), Money.of("200"));

        // when
        OperationResult result = service.handle(cmd);

        // then
        //  // account
        assertThat(result.accountId()).isEqualTo(account.getId());
        assertThat(result.newBalance()).isEqualTo(Money.of("200"));
        assertThat(accountRepository.findById(result.accountId()).getBalance().isEqualTo(Money.of("200"))).isTrue();
        //  // operation
        assertThat(operationRepository.findByAccountIdBetween(account.getId(), before, after).size()).isEqualTo(1);
        var op = operationRepository.findByAccountIdBetween(account.getId(), before, after).get(0);
        assertThat(op.accountId()).isEqualTo(account.getId());
        assertThat(op.amount()).isEqualTo(Money.of("200"));
        assertThat(op.type()).isEqualTo(Operation.Type.DEPOSIT);
    }

    @Test
    void deposit_negative_amount_should_propagate_business_exception() {
        AccountRepository accountRepository = new InMemoryAccountRepository();
        OperationRepository operationRepository = new InMemoryOperationRepository();

        Account account = Account.openCurrent(Money.of("-300"), clock);
        accountRepository.save(account);

        DepositUseCase service = new DepositService(accountRepository, operationRepository);
        DepositCommand cmd = new DepositCommand(account.getId(), Money.of("-200"));

        assertThatThrownBy(() -> service.handle(cmd)).isInstanceOf(BusinessRuleViolationException.class);
        assertThat(operationRepository.findByAccountIdBetween(account.getId(), before, after).size()).isEqualTo(0);
    }

    @Test
    void deposit_exceeding_savings_ceiling_should_propagate_ceiling_exception() {
        AccountRepository accountRepository = new InMemoryAccountRepository();
        OperationRepository operationRepository = new InMemoryOperationRepository();

        DepositUseCase service = new DepositService(accountRepository, operationRepository);
        Account account = Account.openSavings(Money.of("100"), clock);
        accountRepository.save(account);

        DepositCommand cmd = new DepositCommand(account.getId(), Money.of("102"));

        assertThatThrownBy(() -> service.handle(cmd)).isInstanceOf(CeilingExceededException.class);
        assertThat(operationRepository.findByAccountIdBetween(account.getId(), before, after).size()).isEqualTo(0);
    }

    @Test
    void deposit_unknown_account_should_throw_not_found() {
        AccountRepository accountRepository = new InMemoryAccountRepository();
        OperationRepository operationRepository = new InMemoryOperationRepository();

        DepositUseCase service = new DepositService(accountRepository, operationRepository);
        AccountId unknownId = AccountId.newId();

        DepositCommand cmd = new DepositCommand(unknownId, Money.of("350"));

        assertThatThrownBy(() -> service.handle(cmd)).isInstanceOf(AccountNotFoundApplicationException.class);
        assertThat(operationRepository.findByAccountIdBetween(unknownId, before, after).size()).isEqualTo(0);
    }
}
