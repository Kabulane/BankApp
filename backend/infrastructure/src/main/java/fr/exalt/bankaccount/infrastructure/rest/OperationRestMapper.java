package fr.exalt.bankaccount.infrastructure.rest;

import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.infrastructure.rest.dto.OperationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
