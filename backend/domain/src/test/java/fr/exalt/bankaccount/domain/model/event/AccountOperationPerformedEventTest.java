package fr.exalt.bankaccount.domain.model.event;

import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.operation.OperationId;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AccountOperationPerformedEventTest {
    @Test
    void fromOperation_shouldCopyAllFields() {
        // given
        AccountId accountId = AccountId.newId();
        OperationId operationId = OperationId.newId();
        Operation operation = new Operation(operationId, accountId, Money.of("50"), Operation.Type.DEPOSIT, Instant.now(), "This is a test");

        // when
        AccountOperationPerformedEvent event = AccountOperationPerformedEvent.from(operation);

        // then
        assertThat(event.accountId()).isEqualTo(accountId);
        assertThat(event.operationId()).isEqualTo(operationId);
        assertThat(event.amount()).isEqualTo(Money.of("50"));
        assertThat(event.type()).isEqualTo(Operation.Type.DEPOSIT);
        assertThat(event.at()).isEqualTo(operation.at());
    }
}
