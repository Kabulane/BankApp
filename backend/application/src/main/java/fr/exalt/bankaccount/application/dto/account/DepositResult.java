package fr.exalt.bankaccount.application.dto.account;

import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.money.Money;

public record DepositResult(AccountId accountId, Money newBalance, Operation operation) {
}
