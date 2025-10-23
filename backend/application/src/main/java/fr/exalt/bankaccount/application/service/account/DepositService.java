package fr.exalt.bankaccount.application.service.account;

import fr.exalt.bankaccount.application.exception.AccountNotFoundApplicationException;
import fr.exalt.bankaccount.application.port.in.DepositUseCase;
import fr.exalt.bankaccount.application.port.out.AccountRepository;
import fr.exalt.bankaccount.application.dto.account.operation.DepositCommand;
import fr.exalt.bankaccount.application.dto.account.operation.DepositResult;
import fr.exalt.bankaccount.application.port.out.OperationRepository;
import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Transactional
public class DepositService implements DepositUseCase {
    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;

    public DepositService(AccountRepository accountRepository, OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    public DepositResult handle(DepositCommand cmd) {
        Objects.requireNonNull(cmd, "Command must not be null");
        Objects.requireNonNull(cmd.accountId(), "AccountId must no be null");
        Objects.requireNonNull(cmd.deposit(), "Deposit must not be null");

        Account account = accountRepository.findById(cmd.accountId());
        if (account == null) {
            throw new AccountNotFoundApplicationException("Account %s not found".formatted(cmd.accountId()));
        }

        // On délègue la règle métier au domaine (montant > 0 etc...)
        Operation operation = account.deposit(cmd.deposit());
        accountRepository.save(account);
        operationRepository.save(operation);

        return new DepositResult(account.getId(), account.getBalance(), operation);
    }
}
