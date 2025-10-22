package fr.exalt.bankaccount.application.dto.account.openaccount;

import fr.exalt.bankaccount.domain.model.account.AccountId;

public sealed interface OpenAccountResult permits OpenCurrentAccountResult, OpenSavingsAccountResult {
    AccountId accountId();
}
