package fr.exalt.bankaccount.infrastructure.rest.controller;

import fr.exalt.bankaccount.application.dto.account.openaccount.OpenAccountResult;
import fr.exalt.bankaccount.application.dto.account.operation.DepositCommand;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenCurrentAccountCommand;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenSavingsAccountCommand;
import fr.exalt.bankaccount.application.dto.account.operation.OperationResult;
import fr.exalt.bankaccount.application.dto.account.operation.WithdrawCommand;
import fr.exalt.bankaccount.application.port.in.DepositUseCase;
import fr.exalt.bankaccount.application.port.in.OpenCurrentAccountUseCase;
import fr.exalt.bankaccount.application.port.in.OpenSavingsAccountUseCase;
import fr.exalt.bankaccount.application.port.in.WithdrawUseCase;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.infrastructure.rest.mapper.AccountRestMapper;
import fr.exalt.bankaccount.infrastructure.rest.mapper.OperationRestMapper;
import fr.exalt.bankaccount.infrastructure.rest.dto.AccountCreatedResponse;
import fr.exalt.bankaccount.infrastructure.rest.dto.AccountOperationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final OpenCurrentAccountUseCase openCurrentAccountUseCase;
    private final OpenSavingsAccountUseCase openSavingsAccountUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final DepositUseCase depositUseCase;

    private final AccountRestMapper accountRestMapper;
    private final OperationRestMapper operationRestMapper;

    AccountController(OpenCurrentAccountUseCase openCurrentAccountUseCase,
                      OpenSavingsAccountUseCase openSavingsAccountUseCase,
                      WithdrawUseCase withdrawUseCase,
                      DepositUseCase depositUseCase,
                      AccountRestMapper accountRestMapper,
                      OperationRestMapper operationRestMapper) {
        this.openCurrentAccountUseCase = openCurrentAccountUseCase;
        this.openSavingsAccountUseCase = openSavingsAccountUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.depositUseCase = depositUseCase;
        this.accountRestMapper = accountRestMapper;
        this.operationRestMapper = operationRestMapper;
    }

    @PostMapping("/current")
    public ResponseEntity<AccountCreatedResponse> openCurrent(@RequestBody Map<String, Object> body) {
        Money overdraft = Money.of(String.valueOf(body.get("overdraft")));
        OpenAccountResult result = openCurrentAccountUseCase.handle(new OpenCurrentAccountCommand(overdraft));
        return ResponseEntity.status(201).body(accountRestMapper.toCreateResponse(result));
    }

    @PostMapping("/savings")
    public ResponseEntity<AccountCreatedResponse> openSavings(@RequestBody Map<String, Object> body) {
        Money ceiling = Money.of(String.valueOf(body.get("ceiling")));
        OpenAccountResult result = openSavingsAccountUseCase.handle(new OpenSavingsAccountCommand(ceiling));
        return ResponseEntity.status(201).body(accountRestMapper.toCreateResponse(result));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<AccountOperationResponse> withdraw(@PathVariable("id") UUID id, @RequestBody Map<String, Object> body) {
        Money amount = Money.of(String.valueOf(body.get("amount")));
        OperationResult result = withdrawUseCase.handle(new WithdrawCommand(new AccountId(id), amount));
        return ResponseEntity.status(200).body(accountRestMapper.toAccountOperationResponse(result));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<AccountOperationResponse> deposit(@PathVariable("id") UUID id, @RequestBody Map<String, Object> body) {
        Money amount = Money.of(String.valueOf(body.get("amount")));
        OperationResult result = depositUseCase.handle(new DepositCommand(new AccountId(id), amount));
        return ResponseEntity.status(200).body(accountRestMapper.toAccountOperationResponse(result));
    }
}
