package fr.exalt.bankaccount.infrastructure.config;

import fr.exalt.bankaccount.application.port.out.AccountRepository;
import fr.exalt.bankaccount.application.port.out.OperationRepository;
import fr.exalt.bankaccount.application.service.account.DepositService;
import fr.exalt.bankaccount.application.service.account.OpenCurrentAccountService;
import fr.exalt.bankaccount.application.service.account.OpenSavingsAccountService;
import fr.exalt.bankaccount.application.service.account.WithdrawService;
import fr.exalt.bankaccount.application.service.operation.OperationService;
import fr.exalt.bankaccount.infrastructure.rest.mapper.AccountRestMapper;
import fr.exalt.bankaccount.infrastructure.rest.mapper.OperationRestMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;

@Configuration
@EnableTransactionManagement
public class ApplicationServiceConfig {

    // ---- REST mappers (purs, sans Spring)
    @Bean
    public OperationRestMapper operationRestMapper() { return new OperationRestMapper(); }

    @Bean
    public AccountRestMapper accountRestMapper(OperationRestMapper opMapper) {
        return new AccountRestMapper(opMapper);
    }

    // ---- Use cases (purs, sans @Service)
    @Bean
    public OpenCurrentAccountService openCurrentAccountService(AccountRepository accountRepository, Clock clock) {
        return new OpenCurrentAccountService(accountRepository);
    }

    @Bean
    public OpenSavingsAccountService openSavingsAccountService(AccountRepository accountRepository, Clock clock) {
        return new OpenSavingsAccountService(accountRepository);
    }

    @Bean
    public DepositService depositService(AccountRepository accountRepository, OperationRepository operationRepository) {
        return new DepositService(accountRepository, operationRepository);
    }

    @Bean
    public WithdrawService withdrawService(AccountRepository accountRepository, OperationRepository operationRepository) {
        return new WithdrawService(accountRepository, operationRepository);
    }

    @Bean
    public OperationService operationService(OperationRepository operationRepository, Clock clock) {
        return new OperationService(operationRepository, clock);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
