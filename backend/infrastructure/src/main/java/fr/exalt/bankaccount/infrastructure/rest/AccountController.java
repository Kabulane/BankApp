package fr.exalt.bankaccount.infrastructure.rest;

import fr.exalt.bankaccount.application.dto.account.operation.DepositCommand;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenCurrentAccountCommand;
import fr.exalt.bankaccount.application.dto.account.openaccount.OpenSavingsAccountCommand;
import fr.exalt.bankaccount.application.dto.account.operation.WithdrawCommand;
import fr.exalt.bankaccount.application.service.account.DepositService;
import fr.exalt.bankaccount.application.service.account.OpenCurrentAccountService;
import fr.exalt.bankaccount.application.service.account.OpenSavingsAccountService;
import fr.exalt.bankaccount.application.service.account.WithdrawService;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.money.Money;
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
    public ResponseEntity<?> openCurrent(@RequestBody Map<String, Object> body) {
        var overdraft = Money.of(String.valueOf(body.get("overdraft")));
        var result = openCurrentAccountService.handle(new OpenCurrentAccountCommand(overdraft));
        return ResponseEntity.status(201).body(accountRestMapper.toCreateResponse(result));
    }

    @PostMapping("/savings")
    public ResponseEntity<?> openSavings(@RequestBody Map<String, Object> body) {
        var ceiling = Money.of(String.valueOf(body.get("ceiling")));
        var result = openSavingsAccountService.handle(new OpenSavingsAccountCommand(ceiling));
        return ResponseEntity.status(201).body(accountRestMapper.toCreateResponse(result));
    }

    @PostMapping("/{id}/withdraw")
    public Map<String, Object> withdraw(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var amount = Money.of(String.valueOf(body.get("amount")));
        var result = withdrawService.handle(new WithdrawCommand(new AccountId(id), amount));
        return accountRestMapper.toOperationResponse(result);
    }

    @PostMapping("/{id}/deposit")
    public Map<String, Object> deposit(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var amount = Money.of(String.valueOf(body.get("amount")));
        var result = depositService.handle(new DepositCommand(new AccountId(id), amount));
        return accountRestMapper.toOperationResponse(result);
    }
}
