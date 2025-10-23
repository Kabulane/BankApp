package fr.exalt.bankaccount.infrastructure.rest;

import fr.exalt.bankaccount.application.exception.AccountNotFoundApplicationException;
import fr.exalt.bankaccount.application.service.operation.OperationService;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.operation.OperationId;
import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.infrastructure.rest.controller.OperationController;
import fr.exalt.bankaccount.infrastructure.rest.exception.RestExceptionHandler;
import fr.exalt.bankaccount.infrastructure.rest.mapper.OperationRestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@WebMvcTest(controllers = OperationController.class)
@Import({ OperationRestMapper.class, RestExceptionHandler.class })
public class OperationControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    OperationService operationService;

    @Test
    void should_return_monthly_operations_sorted_desc() throws Exception {
        AccountId accountId = new AccountId(UUID.randomUUID());
        List<Operation> ops = List.of(
                new Operation(new OperationId(UUID.randomUUID()), accountId, Money.of("1000"), Operation.Type.DEPOSIT, Instant.parse("2025-10-20T10:00:00Z"), "First"),
                new Operation(new OperationId(UUID.randomUUID()), accountId, Money.of("500"), Operation.Type.WITHDRAWAL, Instant.parse("2025-10-19T10:00:00Z"), "Second")
        );
        when(operationService.getMonthlyOperations(accountId)).thenReturn(ops);

        mockMvc.perform(get("/accounts/{id}/operations", accountId.value()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].amount").value(1000))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"));
    }

    @Test
    void should_return_404_when_account_not_found() throws Exception {
        UUID id = UUID.randomUUID();
        when(operationService.getMonthlyOperations(new AccountId(id)))
                .thenThrow(new AccountNotFoundApplicationException(id.toString()));

        mockMvc.perform(get("/accounts/{id}/operations", id))
                .andExpect(status().isNotFound());
    }
}
