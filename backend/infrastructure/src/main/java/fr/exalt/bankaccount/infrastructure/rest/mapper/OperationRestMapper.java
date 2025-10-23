package fr.exalt.bankaccount.infrastructure.rest.mapper;

import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.infrastructure.rest.dto.operation.OperationResponse;
import org.springframework.stereotype.Component;

@Component
public class OperationRestMapper {

    public OperationResponse toResponse(Operation operation) {
        return new OperationResponse(
                operation.id().value().toString(),
                operation.type().name(),
                operation.amount().value(),
                operation.at(),
                operation.label()
        );
    }
}
