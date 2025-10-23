package fr.exalt.bankaccount.infrastructure.rest.dto.openaccount;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OpenCurrentAccountRequest(
        @NotNull BigDecimal overdraft
) { }
