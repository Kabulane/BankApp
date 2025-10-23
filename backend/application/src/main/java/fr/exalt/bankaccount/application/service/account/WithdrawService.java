package fr.exalt.bankaccount.application.service.account;

import fr.exalt.bankaccount.application.exception.AccountNotFoundApplicationException;
import fr.exalt.bankaccount.application.port.in.WithdrawUseCase;
import fr.exalt.bankaccount.application.port.out.AccountRepository;
import fr.exalt.bankaccount.application.dto.account.operation.WithdrawCommand;
import fr.exalt.bankaccount.application.dto.account.operation.WithdrawResult;
import fr.exalt.bankaccount.application.port.out.OperationRepository;
import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import jakarta.transaction.Transactional;

import java.util.Objects;

@Transactional
public class WithdrawService implements WithdrawUseCase {
    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;

    public WithdrawService(AccountRepository accountRepository, OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    public WithdrawResult handle(WithdrawCommand cmd) {
        Objects.requireNonNull(cmd, "Command must not be null");
        Objects.requireNonNull(cmd.accountId(), "AccountId must no be null");
        Objects.requireNonNull(cmd.withdraw(), "Deposit must not be null");

        Account account = accountRepository.findById(cmd.accountId());
        if (account == null) {
            throw new AccountNotFoundApplicationException("Account %s not found".formatted(cmd.accountId()));
        }

        // On délègue la règle métier au domaine (montant > 0 etc...)
        Operation operation = account.withdraw(cmd.withdraw());
        accountRepository.save(account);
        operationRepository.save(operation);

        return new WithdrawResult(account.getId(), account.getBalance(), operation);
    }
}
