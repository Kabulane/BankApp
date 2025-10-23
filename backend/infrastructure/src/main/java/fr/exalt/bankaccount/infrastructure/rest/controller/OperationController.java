package fr.exalt.bankaccount.infrastructure.rest.controller;

import fr.exalt.bankaccount.application.service.operation.OperationService;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.infrastructure.rest.mapper.OperationRestMapper;
import fr.exalt.bankaccount.infrastructure.rest.dto.OperationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/accounts")
public class OperationController {

    private final OperationService service;
    private final OperationRestMapper mapper;

    OperationController(OperationService service, OperationRestMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/{id}/operations")
    List<OperationResponse> getMonthly(@PathVariable("id") UUID id) {
        List<Operation> ops = service.getMonthlyOperations(new AccountId(id));
        return ops.stream().map(mapper::toResponse).toList();
    }
}
