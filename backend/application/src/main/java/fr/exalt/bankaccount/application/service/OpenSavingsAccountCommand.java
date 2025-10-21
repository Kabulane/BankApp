package fr.exalt.bankaccount.application.service;

import fr.exalt.bankaccount.domain.model.money.Money;

public record OpenSavingsAccountCommand(Money ceiling) {
}
