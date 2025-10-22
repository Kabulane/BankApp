package fr.exalt.bankaccount.application.dto.account.openaccount;

import fr.exalt.bankaccount.domain.model.account.AccountId;

public record OpenSavingsAccountResult(AccountId accountId) implements OpenAccountResult {
}
