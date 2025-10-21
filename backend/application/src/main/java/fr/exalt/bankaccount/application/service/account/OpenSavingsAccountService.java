package fr.exalt.bankaccount.application.service.account;

import fr.exalt.bankaccount.application.port.out.AccountRepository;
import fr.exalt.bankaccount.application.dto.account.OpenSavingsAccountCommand;
import fr.exalt.bankaccount.application.dto.account.OpenSavingsAccountResult;
import fr.exalt.bankaccount.domain.model.account.Account;

import java.time.Clock;
import java.util.Objects;

public class OpenSavingsAccountService {

    private final AccountRepository repository;

    public OpenSavingsAccountService(AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public OpenSavingsAccountResult handle(OpenSavingsAccountCommand command) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(command.ceiling(), "Ceiling must not be null");

        // La règle "Ceiling >= 0 est vérifiée dans le domaine.
        Account account = Account.openSavings(command.ceiling(), Clock.systemUTC());

        repository.save(account);
        return new OpenSavingsAccountResult(account.getId());
    }
}
