package fr.exalt.bankaccount.infrastructure.rest;

import fr.exalt.bankaccount.application.dto.account.openaccount.OpenAccountResult;
import fr.exalt.bankaccount.application.dto.account.operation.OperationResult;
import fr.exalt.bankaccount.application.dto.account.operation.WithdrawResult;

import java.util.Map;

public class AccountRestMapper {
    public Object toCreateResponse(OpenAccountResult result) {
        return null;
    }

    public Map<String, Object> toOperationResponse(OperationResult result) {
        return null;
    }
}
