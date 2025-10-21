package fr.exalt.bankaccount.application.service;

import fr.exalt.bankaccount.domain.model.account.Account;

import java.util.Objects;

public class DepositService {
    private final AccountRepository repository;

    public DepositService(AccountRepository repository) {
        this.repository = repository;
    }

    public DepositResult handle(DepositCommand cmd) {
        Objects.requireNonNull(cmd, "Command must not be null");
        Objects.requireNonNull(cmd.accountId(), "AccountId must no be null");
        Objects.requireNonNull(cmd.deposit(), "Deposit must not be null");

        Account account = repository.findById(cmd.accountId());
        if (account == null) {
            throw new AccountNotFoundException("Account %s not found".formatted(cmd.accountId()));
        }

        // On délègue la règle métier au domaine (montant > 0 etc...)
        account.deposit(cmd.deposit());
        repository.save(account);

        return new DepositResult(account.getId(), account.getBalance());
    }
}
