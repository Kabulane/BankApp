package fr.exalt.bankaccount.infrastructure.rest;

import fr.exalt.bankaccount.application.dto.account.operation.DepositResult;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenCurrentAccountResult;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenSavingsAccountResult;
import fr.exalt.bankaccount.application.dto.account.operation.WithdrawResult;
import fr.exalt.bankaccount.application.exception.AccountNotFoundApplicationException;
import fr.exalt.bankaccount.application.service.account.DepositService;
import fr.exalt.bankaccount.application.service.account.OpenCurrentAccountService;
import fr.exalt.bankaccount.application.service.account.OpenSavingsAccountService;
import fr.exalt.bankaccount.application.service.account.WithdrawService;
import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.exception.BusinessRuleViolationException;
import fr.exalt.bankaccount.domain.model.exception.CeilingExceededException;
import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.exception.InsufficientFundsException;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import({
        AccountRestMapper.class,
        OperationRestMapper.class,
        RestExceptionHandler.class
})
public class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OpenCurrentAccountService openCurrentAccountService;
    @MockBean
    OpenSavingsAccountService openSavingsAccountService;
    @MockBean
    DepositService depositService;
    @MockBean
    WithdrawService withdrawService;

    // ----- Create CURRENT -----
    @Test
    void should_create_current_account() throws Exception {
        Account account = Account.openCurrent(Money.of("-500"), Clock.systemUTC());
        OpenCurrentAccountResult result = new OpenCurrentAccountResult(account.getId());

        when(openCurrentAccountService.handle(any())).thenReturn(result);

        mockMvc.perform(post("/accounts/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"overdraft": -1000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(result.accountId().toString()));
    }

    @Test
    void create_current_should_throw_business_exception () throws Exception {
        when(openCurrentAccountService.handle(any())).thenThrow(new BusinessRuleViolationException("Overdraft limit must be zero or negative"));

        mockMvc.perform(post("/accounts/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"overdraft": 1000}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ----- Create SAVINGS -----

    @Test
    void should_create_SAVINGS_account() throws Exception {
        Account account = Account.openSavings(Money.of("500"), Clock.systemUTC());
        OpenSavingsAccountResult result = new OpenSavingsAccountResult(account.getId());

        when(openSavingsAccountService.handle(any())).thenReturn(result);

        mockMvc.perform(post("/accounts/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ceiling": 1000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(result.accountId().toString()));
    }

    @Test
    void create_savings_should_throw_business_exception () throws Exception {
        when(openSavingsAccountService.handle(any())).thenThrow(new BusinessRuleViolationException("Ceiling must be strictly positive"));

        mockMvc.perform(post("/accounts/savings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ceiling": -1000}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ---------- WITHDRAW (CURRENT) ----------
    @Test
    @DisplayName("POST /accounts/{id}/withdraw - should withdraw and return new balance + operation (200)")
    void withdraw_ok() throws Exception {
        AccountId accountId = AccountId.newId();
        Operation operation = Operation.of(accountId, Money.of("50"), Operation.Type.WITHDRAWAL);
        Money balance = Money.of("250");

        when(withdrawService.handle(any())).thenReturn(new WithdrawResult(accountId, balance, operation));

        mockMvc.perform(post("/accounts/{id}/withdraw", accountId.value().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"amount": 200}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.newBalance").value(250))
                .andExpect(jsonPath("$.operation.id").value(operation.id().value().toString()))
                .andExpect(jsonPath("$.operation.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.operation.amount").value(50))
                .andExpect(jsonPath("$.operation.at").value(operation.at().toString()))
                .andExpect(jsonPath("$.operation.label").value("Withdrawal"));
    }

    @Test
    @DisplayName("POST /accounts/{id}/withdraw - should return 404 when account not found")
    void withdraw_notFound() throws Exception {
        AccountId accountId = AccountId.newId();

        when(withdrawService.handle(any()))
                .thenThrow(new AccountNotFoundApplicationException(accountId.value().toString()));

        mockMvc.perform(post("/accounts/{id}/withdraw", accountId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 50}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /accounts/{id}/withdraw - should return 400 when business rule fails")
    void withdraw_businessRuleViolation() throws Exception {
        AccountId accountId = AccountId.newId();
        when(withdrawService.handle(any()))
                .thenThrow(InsufficientFundsException.class);

        mockMvc.perform(post("/accounts/{id}/withdraw", accountId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 999999}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /accounts/{id}/withdraw - negative amount should return 400")
    void withdraw_negativeAmount_returns400() throws Exception {
        UUID accountId = UUID.randomUUID();

        // On simule que la couche service rejette ce cas métier
        when(withdrawService.handle(any()))
                .thenThrow(new BusinessRuleViolationException("Withdraw amount must be strictly positive"));

        mockMvc.perform(post("/accounts/{id}/withdraw", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -1}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- DEPOSIT (SAVINGS) ----------

    @Test
    @DisplayName("POST /accounts/{id}/deposit - should deposit and return new balance + operation (200)")
    void deposit_ok() throws Exception {
        AccountId accountId = AccountId.newId();
        Operation operation = Operation.of(accountId, Money.of("50"), Operation.Type.DEPOSIT);
        Money balance = Money.of("250");

        when(depositService.handle(any())).thenReturn(new DepositResult(accountId, balance, operation));

        mockMvc.perform(post("/accounts/{id}/deposit", accountId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"amount": 50}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.newBalance").value(250))
                .andExpect(jsonPath("$.operation.id").value(operation.id().value().toString()))
                .andExpect(jsonPath("$.operation.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.operation.amount").value(50))
                .andExpect(jsonPath("$.operation.at").value(operation.at().toString()))
                .andExpect(jsonPath("$.operation.label").value("Deposit"));
    }

    @Test
    @DisplayName("POST /accounts/{id}/deposit - should return 404 when account not found")
    void deposit_notFound() throws Exception {
        AccountId accountId = AccountId.newId();

        when(depositService.handle(any()))
                .thenThrow(new AccountNotFoundApplicationException(accountId.value().toString()));

        mockMvc.perform(post("/accounts/{id}/deposit", accountId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 50}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /accounts/{id}/deposit - should return 400 when business rule fails")
    void deposit_businessRuleViolation() throws Exception {
        AccountId accountId = AccountId.newId();
        when(depositService.handle(any()))
                .thenThrow(CeilingExceededException.class);

        mockMvc.perform(post("/accounts/{id}/deposit", accountId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 999999}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /accounts/{id}/deposit - negative amount should return 400")
    void deposit_negativeAmount_returns400() throws Exception {
        UUID accountId = UUID.randomUUID();

        when(depositService.handle(any()))
                .thenThrow(new DomainException("Deposit amount must be strictly positive"));

        mockMvc.perform(post("/accounts/{id}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -50}"))
                .andExpect(status().isBadRequest());
    }

    // --------- Cas Génériques -----------

    @Test
    @DisplayName("POST /accounts/current - should return 400 when body is missing/invalid JSON")
    void badRequest_whenBodyMissing() throws Exception {
        mockMvc.perform(post("/accounts/current").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
