package fr.exalt.bankaccount.application.service;

import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.money.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OpenSavingsAccountServiceTest {

    // Stub simple pour capturer ce que le service sauvegarde
    static final class InMemoryAccountRepository implements  AccountRepository {
        Account saved;
        @Override public Account save(Account account) {this.saved = account; return account;}
        @Override public Account findById(AccountId id) {return null;}
    }

    @Test
    void open_savings_account_with_ceiling_should_create_persisted_zero_balance_account() {
        // given
        InMemoryAccountRepository repo = new InMemoryAccountRepository();
        OpenSavingsAccountService service = new OpenSavingsAccountService(repo);

        OpenSavingsAccountCommand cmd = new OpenSavingsAccountCommand(Money.of("2000")); // Découvert autorisé

        // when
        OpenSavingsAccountResult result = service.handle(cmd);

        // then
        assertThat(result.accountId()).isNotNull();
        assertThat(repo.saved).isNotNull();
        assertThat(repo.saved.getType()).isEqualTo(Account.Type.SAVINGS);
        assertThat(repo.saved.getBalance()).isEqualTo(Money.zero());
        assertThat(repo.saved.getCeiling()).isEqualTo(Money.of("2000"));
    }
}
