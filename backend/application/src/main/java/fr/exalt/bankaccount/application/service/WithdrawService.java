package fr.exalt.bankaccount.application.service;

import fr.exalt.bankaccount.domain.model.account.Account;

import java.util.Objects;

public class WithdrawService {
    private final AccountRepository repository;

    public WithdrawService(AccountRepository repository) {
        this.repository = repository;
    }

    public WithdrawResult handle(WithdrawCommand cmd) {
        Objects.requireNonNull(cmd, "Command must not be null");
        Objects.requireNonNull(cmd.accountId(), "AccountId must no be null");
        Objects.requireNonNull(cmd.withdraw(), "Deposit must not be null");

        Account account = repository.findById(cmd.accountId());
        if (account == null) {
            throw new AccountNotFoundException("Account %s not found".formatted(cmd.accountId()));
        }

        // On délègue la règle métier au domaine (montant > 0 etc...)
        account.withdraw(cmd.withdraw());
        repository.save(account);

        return new WithdrawResult(account.getId(), account.getBalance());
    }
}
