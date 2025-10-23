package fr.exalt.bankaccount.infrastructure.rest.mapper;

import fr.exalt.bankaccount.application.dto.account.openaccount.OpenAccountResult;
import fr.exalt.bankaccount.application.dto.account.operation.OperationResult;
import fr.exalt.bankaccount.infrastructure.rest.dto.openaccount.AccountCreatedResponse;
import fr.exalt.bankaccount.infrastructure.rest.dto.operation.AccountOperationResponse;
import fr.exalt.bankaccount.infrastructure.rest.dto.operation.OperationResponse;

public class AccountRestMapper {

    private final OperationRestMapper operationRestMapper;

    public AccountRestMapper (OperationRestMapper operationRestMapper) {
        this.operationRestMapper = operationRestMapper;
    }

    public AccountCreatedResponse toCreateResponse(OpenAccountResult result) {
        return new AccountCreatedResponse(result.accountId().toString());
    }

    public AccountOperationResponse toAccountOperationResponse(OperationResult result) {
        // {
        //   "accountId": "<uuid>",
        //   "newBalance": 250,
        //   "operation": { "id": "...", "type": "DEPOSIT|WITHDRAWAL", "amount": 50, "at": "...", "label": "..." }
        // }
        OperationResponse op = operationRestMapper.toResponse(result.operation());
        return new AccountOperationResponse(
                result.accountId().toString(),
                result.newBalance().value(),
                op
        );
    }
}
