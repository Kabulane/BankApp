package fr.exalt.bankaccount.application.port.in;

import fr.exalt.bankaccount.application.dto.account.operation.DepositCommand;
import fr.exalt.bankaccount.application.dto.account.operation.OperationResult;

public interface DepositUseCase {
    OperationResult handle(DepositCommand depositCommand);
}
