package fr.exalt.bankaccount.application.service.account;

import fr.exalt.bankaccount.application.port.in.OpenSavingsAccountUseCase;
import fr.exalt.bankaccount.application.port.out.AccountRepository;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenSavingsAccountCommand;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenSavingsAccountResult;
import fr.exalt.bankaccount.domain.model.account.Account;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.Objects;

@Transactional
public class OpenSavingsAccountService implements OpenSavingsAccountUseCase {

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
