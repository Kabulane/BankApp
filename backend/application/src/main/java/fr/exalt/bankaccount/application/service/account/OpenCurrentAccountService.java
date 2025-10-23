package fr.exalt.bankaccount.application.service.account;

import fr.exalt.bankaccount.application.port.in.OpenCurrentAccountUseCase;
import fr.exalt.bankaccount.application.port.out.AccountRepository;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenCurrentAccountCommand;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenCurrentAccountResult;
import fr.exalt.bankaccount.domain.model.account.Account;
import jakarta.transaction.Transactional;


import java.time.Clock;
import java.util.Objects;

@Transactional
public class OpenCurrentAccountService implements OpenCurrentAccountUseCase {

    private final AccountRepository repository;

    public OpenCurrentAccountService(AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public OpenCurrentAccountResult handle(OpenCurrentAccountCommand command) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(command.overdraft(), "Overdraft must not be null");

        // La règle "Overdraft <= 0 est vérifiée dans le domaine.
        Account account = Account.openCurrent(command.overdraft(), Clock.systemUTC());

        repository.save(account);
        return new OpenCurrentAccountResult(account.getId());
    }
}
