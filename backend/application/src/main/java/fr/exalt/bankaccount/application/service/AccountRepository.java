package fr.exalt.bankaccount.application.service;

import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.AccountId;

public interface AccountRepository {
    Account save(Account account);
    Account findById(AccountId id);
}
