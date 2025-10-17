package fr.exalt.bankaccount.domain.model.account;

import java.util.UUID;

public record AccountId(UUID value) {
    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }
}
