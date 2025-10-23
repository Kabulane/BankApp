package fr.exalt.bankaccount.infrastructure.rest.dto.operation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AmountRequest(@NotNull BigDecimal amount) {
}
