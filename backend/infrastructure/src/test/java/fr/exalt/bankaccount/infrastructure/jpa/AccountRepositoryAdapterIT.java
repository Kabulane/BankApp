package fr.exalt.bankaccount.infrastructure.jpa;

import fr.exalt.bankaccount.application.exception.AccountNotFoundApplicationException;
import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.infrastructure.TestJpaConfig;
import fr.exalt.bankaccount.infrastructure.jpa.adapter.AccountRepositoryAdapter;
import fr.exalt.bankaccount.infrastructure.jpa.entity.AccountEntity;
import fr.exalt.bankaccount.infrastructure.jpa.mapper.AccountMapper;
import fr.exalt.bankaccount.infrastructure.jpa.spring.AccountJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import({
        AccountRepositoryAdapter.class,
        AccountEntity.class,
        AccountMapper.class,
        TestJpaConfig.class
})
public class AccountRepositoryAdapterIT {

    @Autowired
    AccountRepositoryAdapter adapter;
    private final Clock clock = Clock.systemUTC();

    @Test
    @DisplayName("Save + findById (Current) + persiste balance, overdraft et rehydrate correctement")
    void persist_and_rehydrate_current_account() {
        // given
        Account current = Account.openCurrent(Money.of("-200"), Clock.systemUTC());

        // When
        adapter.save(current);
        Account found = adapter.findById(current.getId());

        // then
        assertThat(found.getId()).isEqualTo(current.getId());
        assertThat(found.getType()).isEqualTo(current.getType());
        assertThat(found.getBalance()).isEqualTo(current.getBalance());
        assertThat(found.getCeiling()).isEqualTo(current.getCeiling());
        assertThat(found.getOverdraft()).isEqualTo(current.getOverdraft());
    }

    @Test
    @DisplayName("save + findById (Savings) : persiste balance ceiling et rehydrate correctement")
    void persist_and_rehydrate_savings_account() {
        // given
        Account savings = Account.openSavings(Money.of("2000"), Clock.systemUTC());

        // When
        adapter.save(savings);
        Account found = adapter.findById(savings.getId());

        // then
        assertThat(found.getId()).isEqualTo(savings.getId());
        assertThat(found.getType()).isEqualTo(savings.getType());
        assertThat(found.getBalance()).isEqualTo(savings.getBalance());
        assertThat(found.getCeiling()).isEqualTo(savings.getCeiling());
        assertThat(found.getOverdraft()).isEqualTo(savings.getOverdraft());
    }

    @Test
@DisplayName("FindById : lève AccountNotFoundApplicationException si absent")
    void find_by_id_throws_when_missing() {
        AccountId accountId = AccountId.newId();
        assertThatThrownBy(() -> adapter.findById(accountId))
                .isInstanceOf(AccountNotFoundApplicationException.class)
                .hasMessageContaining(accountId.value().toString());

    }
}
