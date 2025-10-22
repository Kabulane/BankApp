package fr.exalt.bankaccount.infrastructure.jpa.mapper;

import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.operation.OperationId;
import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.infrastructure.jpa.entity.OperationEntity;

public class OperationMapper {
    public OperationEntity toEntity(Operation operation) {
        return OperationEntity.create(
            operation.id().value(),
            operation.accountId().value(),
            operation.amount().value(),
            operation.type().name(),
            operation.at(),
            operation.label()
        );
    }

    public Operation toDomain(OperationEntity entity) {
        return new Operation(
            new OperationId(entity.getId()),
            new AccountId(entity.getAccountId()),
            Money.of(entity.getAmount().toString()),
            Operation.Type.valueOf(entity.getType()),
            entity.getAt(),
            entity.getLabel()
        );

    }
}
