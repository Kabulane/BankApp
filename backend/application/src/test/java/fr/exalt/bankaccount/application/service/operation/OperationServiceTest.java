package fr.exalt.bankaccount.application.service.operation;

import fr.exalt.bankaccount.application.port.out.OperationRepository;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.operation.OperationId;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class OperationServiceTest {

    private static final class InMemoryOperationRepository implements OperationRepository {
        private final List<Operation> store = new ArrayList<>();

        public Operation save(Operation operation) {
            store.add(operation);
            return operation;
        }

        @Override
        public List<Operation> findByAccountIdBetween(AccountId accountId, Instant from, Instant to) {
            return store.stream()
                    .filter(operation -> operation.accountId().equals(accountId))
                    .filter(operation -> !operation.at().isBefore(from) && !operation.at().isAfter(to))
                    .sorted(Comparator.comparing(Operation::at).reversed())
                    .toList();
        }
    }

    @Test
    @DisplayName("Récupère les opération du mois glissant pour le compte donné")
    void get_monthly_operations() {
        // now = 2025-10-01T10:00:00Z
        Clock fixedClock = Clock.fixed(Instant.parse("2025-10-01T10:00:00Z"), ZoneOffset.UTC);
        InMemoryOperationRepository repo = new InMemoryOperationRepository();  // mini implémentation dédiée au test
        OperationService operationService = new OperationService(repo, fixedClock);

        AccountId accountA = new AccountId(UUID.randomUUID());
        AccountId accountB = new AccountId(UUID.randomUUID());

        // A - dans la fenêtre
        OperationId opA = new OperationId(UUID.randomUUID());
        OperationId opB = new OperationId(UUID.randomUUID());

        repo.save(new Operation(opA, accountA, Money.of("50"), Operation.Type.DEPOSIT,
                Instant.parse("2025-09-30T12:00:00Z"), "")); // proche de now
        repo.save(new Operation(opB, accountA, Money.of("50"), Operation.Type.DEPOSIT,
                Instant.parse("2025-09-01T10:00:00Z"), "")); // borne from inclusive

        // A - hors fenêtre
        OperationId opC = new OperationId(UUID.randomUUID());

        repo.save(new Operation(opC, accountA, Money.of("50"), Operation.Type.DEPOSIT,
                Instant.parse("2025-08-31T23:59:59Z"), ""));

        // B - dans la fenêtre (doit être ignorée car autre compte)
        repo.save(new Operation(new OperationId(UUID.randomUUID()), accountB, Money.of("50"), Operation.Type.DEPOSIT,
                Instant.parse("2025-09-15T12:00:00Z"), ""));

        List<Operation> result = operationService.getMonthlyOperations(accountA);

        // Le mini-repo trie DESC par date pour la lisibilité des assertions
        assertThat(result)
                .extracting(Operation::id)
                .containsExactly(opA, opB);
    }

    @Test
    @DisplayName("Retourne une liste vide si aucune opération dans la fenêtre")
    void get_empty_list() {
        Clock fixedClock = Clock.fixed(Instant.parse("2025-10-01T10:00:00Z"), ZoneOffset.UTC);
        OperationRepository repo = new InMemoryOperationRepository();
        OperationService service = new OperationService(repo, fixedClock);

        AccountId accountA = new AccountId(UUID.randomUUID());
        // uniquement des opérations trop anciennes
        repo.save(new Operation(new OperationId(UUID.randomUUID()), accountA, Money.of("50"), Operation.Type.DEPOSIT, Instant.parse("2025-08-01T10:00:00Z"), ""));

        var result = service.getMonthlyOperations(accountA);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Cas non passant : accountId null -> IllegalArgumentException")
    void should_throw_illegal_exception() {
        Clock fixedClock = Clock.fixed(Instant.parse("2025-10-01T10:00:00Z"), ZoneOffset.UTC);
        OperationRepository repo = new InMemoryOperationRepository();
        OperationService service = new OperationService(repo, fixedClock);

        assertThatThrownBy(() -> service.getMonthlyOperations(null))
                .isInstanceOf(NullPointerException.class) // Objects.requireNonNull lève un NPE
                .hasMessageContaining("accountId");
    }

}
