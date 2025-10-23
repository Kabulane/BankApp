package fr.exalt.bankaccount.infrastructure.rest.dto;

import java.math.BigDecimal;

/**
 * Réponse pour deposit/withdraw :
 * {
 *   "accountId": "<uuid>",
 *   "newBalance": 250,
 *   "operation": { ...OperationResponse... }
 * }
 */
public record AccountOperationResponse (String accountId, BigDecimal newBalance, OperationResponse operation){
}
