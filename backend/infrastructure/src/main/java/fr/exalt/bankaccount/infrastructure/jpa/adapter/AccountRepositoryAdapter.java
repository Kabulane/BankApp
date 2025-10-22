package fr.exalt.bankaccount.infrastructure.jpa.adapter;

import fr.exalt.bankaccount.application.exception.AccountNotFoundApplicationException;
import fr.exalt.bankaccount.application.port.out.AccountRepository;
import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.infrastructure.jpa.mapper.AccountMapper;
import fr.exalt.bankaccount.infrastructure.jpa.spring.AccountJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Clock;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final AccountMapper accountMapper = new AccountMapper();
    private final Clock clock;

    public AccountRepositoryAdapter (AccountJpaRepository jpa, Clock clock) {
        this.accountJpaRepository = jpa;
        this.clock = clock;
    }

    @Override
    public Account findById(AccountId accountId) {
        return accountJpaRepository.findById(accountId.value())
                .map(entity -> accountMapper.toDomain(entity, clock))
                .orElseThrow(
                        () -> new AccountNotFoundApplicationException("Account not found: " + accountId)
                );
    }

    @Override
    public Account save(Account account) {
        accountJpaRepository.save(accountMapper.toEntity(account));
        return account;
    }
}
