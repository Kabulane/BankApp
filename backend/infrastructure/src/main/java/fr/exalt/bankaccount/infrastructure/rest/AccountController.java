package fr.exalt.bankaccount.infrastructure.rest;

import fr.exalt.bankaccount.application.dto.account.openaccount.OpenAccountResult;
import fr.exalt.bankaccount.application.dto.account.operation.DepositCommand;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenCurrentAccountCommand;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenSavingsAccountCommand;
import fr.exalt.bankaccount.application.dto.account.operation.OperationResult;
import fr.exalt.bankaccount.application.dto.account.operation.WithdrawCommand;
import fr.exalt.bankaccount.application.service.account.DepositService;
import fr.exalt.bankaccount.application.service.account.OpenCurrentAccountService;
import fr.exalt.bankaccount.application.service.account.OpenSavingsAccountService;
import fr.exalt.bankaccount.application.service.account.WithdrawService;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.infrastructure.rest.dto.AccountCreatedResponse;
import fr.exalt.bankaccount.infrastructure.rest.dto.AccountOperationResponse;
import fr.exalt.bankaccount.infrastructure.rest.dto.OperationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final OpenCurrentAccountService openCurrentAccountService;
    private final OpenSavingsAccountService openSavingsAccountService;
    private final WithdrawService withdrawService;
    private final DepositService depositService;

    private final AccountRestMapper accountRestMapper;
    private final OperationRestMapper operationRestMapper;

    AccountController(OpenCurrentAccountService openCurrentAccountService,
                      OpenSavingsAccountService openSavingsAccountService,
                      WithdrawService withdrawService,
                      DepositService depositService,
                      AccountRestMapper accountRestMapper,
                      OperationRestMapper operationRestMapper) {
        this.openCurrentAccountService = openCurrentAccountService;
        this.openSavingsAccountService = openSavingsAccountService;
        this.withdrawService = withdrawService;
        this.depositService = depositService;
        this.accountRestMapper = accountRestMapper;
        this.operationRestMapper = operationRestMapper;
    }

    @PostMapping("/current")
    public ResponseEntity<AccountCreatedResponse> openCurrent(@RequestBody Map<String, Object> body) {
        Money overdraft = Money.of(String.valueOf(body.get("overdraft")));
        OpenAccountResult result = openCurrentAccountService.handle(new OpenCurrentAccountCommand(overdraft));
        return ResponseEntity.status(201).body(accountRestMapper.toCreateResponse(result));
    }

    @PostMapping("/savings")
    public ResponseEntity<AccountCreatedResponse> openSavings(@RequestBody Map<String, Object> body) {
        Money ceiling = Money.of(String.valueOf(body.get("ceiling")));
        OpenAccountResult result = openSavingsAccountService.handle(new OpenSavingsAccountCommand(ceiling));
        return ResponseEntity.status(201).body(accountRestMapper.toCreateResponse(result));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<AccountOperationResponse> withdraw(@PathVariable("id") UUID id, @RequestBody Map<String, Object> body) {
        Money amount = Money.of(String.valueOf(body.get("amount")));
        OperationResult result = withdrawService.handle(new WithdrawCommand(new AccountId(id), amount));
        return ResponseEntity.status(200).body(accountRestMapper.toAccountOperationResponse(result));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<AccountOperationResponse> deposit(@PathVariable("id") UUID id, @RequestBody Map<String, Object> body) {
        Money amount = Money.of(String.valueOf(body.get("amount")));
        OperationResult result = depositService.handle(new DepositCommand(new AccountId(id), amount));
        return ResponseEntity.status(200).body(accountRestMapper.toAccountOperationResponse(result));
    }
}
