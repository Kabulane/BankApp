package fr.exalt.bankaccount.application.port.in;

import fr.exalt.bankaccount.application.dto.account.operation.OperationResult;
import fr.exalt.bankaccount.application.dto.account.operation.WithdrawCommand;

public interface WithdrawUseCase {
    OperationResult handle(WithdrawCommand withdrawCommand);
}
