package fr.exalt.bankaccount.application.service;

import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.money.Money;

public record WithdrawCommand(AccountId accountId, Money withdraw) {
}
