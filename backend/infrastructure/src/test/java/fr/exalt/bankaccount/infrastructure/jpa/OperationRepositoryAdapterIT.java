package fr.exalt.bankaccount.infrastructure.jpa;

import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.operation.OperationId;
import fr.exalt.bankaccount.infrastructure.TestBootConfig;
import fr.exalt.bankaccount.infrastructure.jpa.adapter.OperationRepositoryAdapter;
import fr.exalt.bankaccount.infrastructure.jpa.entity.OperationEntity;
import fr.exalt.bankaccount.infrastructure.jpa.spring.OperationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestBootConfig.class)
@AutoConfigureTestDatabase // force une DB embarquée (H2) si dispo
@ActiveProfiles("test")
public class OperationRepositoryAdapterIT {

    @Autowired
    OperationRepositoryAdapter adapter;
    @Autowired
    OperationJpaRepository jpa;
    @Autowired
    Clock clock;

    private final AccountId accountA = AccountId.newId();
    private final AccountId accountB = AccountId.newId();

    @BeforeEach
    void seed() {
        Instant now = Instant.now(clock);

        // 31 jours → hors fenêtre
        jpa.save(OperationEntity.create(
                OperationId.newId().value(), accountA.value(), new BigDecimal(10), "DEPOSIT",
                now.minus(31, ChronoUnit.DAYS), "old"));

        // 5j, 1j, 10j → Tri attendu DESC
        jpa.save(OperationEntity.create(
                OperationId.newId().value(), accountA.value(), new BigDecimal(10), "DEPOSIT",
                now.minus(5, ChronoUnit.DAYS), "five"));
        jpa.save(OperationEntity.create(
                OperationId.newId().value(), accountA.value(), new BigDecimal(10), "WITHDRAWAL",
                now.minus(1, ChronoUnit.DAYS), "one"));
        jpa.save(OperationEntity.create(
                OperationId.newId().value(), accountA.value(), new BigDecimal(10), "DEPOSIT",
                now.minus(10, ChronoUnit.DAYS), "ten"));

        // Bruit autre compte dans la fenêtre
        jpa.save(OperationEntity.create(
                OperationId.newId().value(), accountB.value(), new BigDecimal(10), "DEPOSIT",
                now.minus(12, ChronoUnit.DAYS), "other"));
    }

    @Test
    @DisplayName("findByAccountIdBetween: ne retourne que les 30 derniers jours, triés DESC par date de création")
    void should_return_last_30_days_desc() {
        Instant to = Instant.now(clock);
        Instant from = to.minus(30, ChronoUnit.DAYS);

        List<Operation> ops = adapter.findByAccountIdBetween(accountA, from, to);

        assertThat(ops).hasSize(3);
        assertThat(ops).extracting(Operation::label).containsExactly("one", "five", "ten"); // DESC
        assertThat(ops).allMatch(o -> o.accountId().equals(accountA));
    }


}
