package fr.exalt.bankaccount.infrastructure.rest.dto.operation;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(@NotNull BigDecimal amount) {
}
