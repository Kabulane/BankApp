package fr.exalt.bankaccount.application.dto.account;

import fr.exalt.bankaccount.domain.model.money.Money;

public record OpenCurrentAccountCommand(Money overdraft) {
}
