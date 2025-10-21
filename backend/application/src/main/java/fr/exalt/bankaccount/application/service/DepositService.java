package fr.exalt.bankaccount.application.service;

import fr.exalt.bankaccount.domain.model.account.Account;

public class DepositService {
    private final AccountRepository repository;

    public DepositService(AccountRepository repository) {
        this.repository = repository;
    }

    public DepositResult handle(DepositCommand cmd) {
        return null;
    }
}
