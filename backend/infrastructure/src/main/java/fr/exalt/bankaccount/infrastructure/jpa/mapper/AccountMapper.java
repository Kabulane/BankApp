package fr.exalt.bankaccount.infrastructure.jpa.mapper;

import fr.exalt.bankaccount.domain.model.account.Account;
import fr.exalt.bankaccount.domain.model.account.AccountId;
import fr.exalt.bankaccount.domain.model.money.Money;
import fr.exalt.bankaccount.infrastructure.jpa.entity.AccountEntity;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

public class AccountMapper {

    public AccountEntity toEntity(Account account) {
        UUID accountUuid = account.getId().value();
        String type = account.getType().name();
        BigDecimal balance = account.getBalance().value();
        BigDecimal overdraft = null;
        BigDecimal ceiling = null;

        if (account.getType() == Account.Type.CURRENT) {
            // découvert <= 0 ; plafond NULL
            overdraft = account.getOverdraft().value();
        } else {
            // découvert null ; plafond > 0
            ceiling = account.getCeiling().value();
        }

        return AccountEntity.create(accountUuid, type, balance, overdraft, ceiling);
    }

    public Account toDomain(AccountEntity accountEntity, Clock clock) {

        AccountId accountId = new AccountId(accountEntity.getId());
        Account.Type type = Account.Type.valueOf(accountEntity.getType());
        Money balance = Money.of(accountEntity.getBalance().toString());
        Money overdraft = type == Account.Type.CURRENT ?
                Money.of(accountEntity.getOverdraft().toString()) : null;
        Money ceiling = type == Account.Type.SAVINGS ?
                Money.of(accountEntity.getCeiling().toString()) : null;

        return Account.rehydrate(accountId, type, balance, overdraft, ceiling, clock);
    }
}
