package fr.exalt.bankaccount.infrastructure.rest.dto.operation;

import java.math.BigDecimal;
import java.time.Instant;

public record OperationResponse(
        String id,
        String type,
        BigDecimal amount,
        Instant at,
        String label
) {
}
