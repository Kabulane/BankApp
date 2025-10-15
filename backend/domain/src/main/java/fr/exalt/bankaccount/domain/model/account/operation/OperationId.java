package fr.exalt.bankaccount.domain.model.account.operation;

import java.util.UUID;

public record OperationId(UUID value) {

    public static OperationId newId() {
        return new OperationId(UUID.randomUUID());
    }
}
