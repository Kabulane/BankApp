package fr.exalt.bankaccount.infrastructure.rest.dto.openaccount;

import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OpenCurrentAccountRequest(
        @NotNull BigDecimal overdraft
) { }
