package fr.exalt.bankaccount.application.dto.account.operation;

public sealed interface OperationResult permits WithdrawResult, DepositResult {
}
