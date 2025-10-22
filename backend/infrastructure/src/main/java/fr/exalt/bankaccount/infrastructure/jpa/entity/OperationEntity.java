package fr.exalt.bankaccount.infrastructure.jpa.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "operations", indexes= {
        @Index(name = "idx_ops_account_at", columnList = "account_id, at")
})
public class OperationEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String type; // DEPOSIT / WITHDRAW

    @Column(nullable = false)
    private Instant at;

    @Column
    private String label;

    protected OperationEntity() {}

    private OperationEntity(UUID id, UUID accountId, BigDecimal amount, String type, Instant at, String label) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.at = at;
        this.label = label;
    }


    public static OperationEntity create(UUID id, UUID accountId, BigDecimal amount, String type, Instant at, String label) {
        return new OperationEntity(id, accountId, amount, type, at, label);
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public String getType() { return type; }
    public Instant getAt() { return at; }
    public String getLabel() { return label; }
}
