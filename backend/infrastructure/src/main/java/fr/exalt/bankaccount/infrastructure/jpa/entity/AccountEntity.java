package fr.exalt.bankaccount.infrastructure.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Version;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="accounts")
public class AccountEntity {

    @Id
    private UUID id;

    @Version
    private long version;

    @Column(nullable = false)
    private String type; // "SAVINGS" / "CURRENT"

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal balance;

    @Column(precision= 19, scale = 2)
    private BigDecimal overdraft;

    @Column(precision= 19, scale = 2)
    private BigDecimal ceiling;

    protected AccountEntity() {}

    private AccountEntity(UUID id, String type, BigDecimal balance, BigDecimal overdraft, BigDecimal ceiling) {
        this.id = id;
        this.type = type;
        this.balance = balance;
        this.overdraft = overdraft;
        this.ceiling = ceiling;
    }

    public static AccountEntity create (UUID id, String type, BigDecimal balance, BigDecimal overdraft, BigDecimal ceiling) {
        return new AccountEntity(id, type, balance, overdraft, ceiling);
    }

    public UUID getId() { return id; }
    public String getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getOverdraft() { return overdraft; }
    public BigDecimal getCeiling() { return ceiling; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setOverdraft(BigDecimal overdraft) { this.overdraft = overdraft; }
    public void setCeiling(BigDecimal ceiling) { this.ceiling = ceiling; }
}
