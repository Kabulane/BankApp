package fr.exalt.bankaccount.domain.model.account;

import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.CeilingPolicy;
import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.FixedCeiling;
import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.NoCeiling;
import fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy.FixedOverdraft;
import fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy.NoOverdraft;
import fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy.OverdraftPolicy;
import fr.exalt.bankaccount.domain.model.exception.DomainException;
import fr.exalt.bankaccount.domain.model.money.Money;

import java.time.Clock;
import java.util.*;

/**
 * Agrégat Account.
 * <p>
 * Remarque d'architecture :
 * -------------------------
 * Les règles et opérations diffèrent selon le type (CURRENT / SAVINGS),
 * mais la variabilité reste gérée via des policies (OverdraftPolicy, CeilingPolicy).
 * <br/>
 * Si l'application évolue (nouveaux types de comptes, règles plus divergentes),
 * il pourra être pertinent d'introduire deux sous-types dédiés :
 * - CurrentAccount
 * - SavingsAccount
 * </p>
 * afin de bénéficier d'une meilleure sécurité de type et d'une lisibilité accrue.
*/
public class Account {

    public enum Type { CURRENT, SAVINGS }

    private final AccountId id;
    private final Type type;
    private final Clock clock;

    // Balance courante
    private Money balance;

    // Policies
    private CeilingPolicy ceilingPolicy;
    private OverdraftPolicy overdraftPolicy;

    // Historique (stocké en ordre inverse chrono : plus récent d'abord)
    private final List<Operation> operations;

    // --------------------------
    // Constructeur privé interne
    // --------------------------
    private Account(AccountId id,
                    Type type,
                    Clock clock,
                    Money balance,
                    OverdraftPolicy overdraftPolicy,
                    CeilingPolicy ceilingPolicy,
                    List<Operation> initialOperations) {

        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.clock = (clock == null) ? Clock.systemUTC() : clock;

        this.balance = (balance == null) ? Money.zero() : balance;

        this.overdraftPolicy = Objects.requireNonNull(overdraftPolicy);
        this.ceilingPolicy = Objects.requireNonNull(ceilingPolicy);

        // tri inverse chrono
        this.operations = new ArrayList<>();
        if (initialOperations != null && !initialOperations.isEmpty()) {
            this.operations.addAll(initialOperations);
            this.operations.sort(Comparator.comparing(Operation::at).reversed());
        }
    }

    // ---------------------
    // Factories d'ouverture
    // ---------------------

    public static Account openCurrent(Money authorizedOverdaft, Clock clock) {
        // Paramétrage des policies
        if (authorizedOverdaft == null || authorizedOverdaft.isGreaterThan(Money.zero())) {
            throw new DomainException("Overdraft limit must be zero or negative");
        }

        return new Account(
                AccountId.newId(), Type.CURRENT, clock,  Money.zero(),
                new FixedOverdraft(authorizedOverdaft), new NoCeiling(), null);

    }
    public static Account openSavings(Money ceiling, Clock clock) {

        if (ceiling == null || ceiling.isLessThanOrEqual(Money.zero())) {
            throw new DomainException("Ceiling must be strictly positive.");
        }

        return new Account(
                AccountId.newId(), Type.SAVINGS, clock,  Money.zero(),
                new NoOverdraft(), new FixedCeiling(ceiling), null);
    }

    // ---------- Réhydratation ----------

    public static Account rehydrate(AccountId id,
                                    Type type,
                                    Money currentBalance,
                                    List<Operation> pastOperations,
                                    Money overdraftOrNull,   // CURRENT: <= 0 ; SAVINGS: null
                                    Money ceilingOrNull,     // SAVINGS: > 0 ; CURRENT: null
                                    Clock clock) {
        if (type == Type.CURRENT) {
            if (overdraftOrNull == null || overdraftOrNull.isGreaterThan(Money.zero())) {
                throw new DomainException("Overdraft limit must be zero or negative.");
            }
            return new Account(
                    id, type, clock, currentBalance,
                    new FixedOverdraft(overdraftOrNull),
                    new NoCeiling(),
                    pastOperations
            );
        } else {
            if (ceilingOrNull == null || ceilingOrNull.isLessThanOrEqual(Money.zero())) {
                throw new DomainException("Ceiling must be strictly positive.");
            }
            return new Account(
                    id, type, clock, currentBalance,
                    new NoOverdraft(),
                    new FixedCeiling(ceilingOrNull),
                    pastOperations
            );
        }
    }

    // ---------- Commandes ----------

    public void deposit(Money amount) {
        if (amount == null || amount.isLessThanOrEqual(Money.zero())) {
            throw new DomainException("Deposit amount must be strictly positive");
        }
        ceilingPolicy.validateDeposit(this.balance, amount);

        Operation op = Operation.of(this.id, amount, Operation.Type.DEPOSIT);
        this.balance = op.applyTo(this.balance);
        addOperationToOperationList(op);
    }

    public void withdraw(Money amount) {
        if (amount == null || amount.isLessThanOrEqual(Money.zero())) {
            throw new DomainException("Withdraw amount must be strictly positive");
        }
        overdraftPolicy.validateWithdraw(this.balance, amount);

        Operation op = Operation.of(this.id, amount, Operation.Type.WITHDRAWAL);
        this.balance = op.applyTo(this.balance);
        addOperationToOperationList(op);
    }

    /** CURRENT uniquement. */
    public void adjustOverdraftLimit(Money newOverdraftLimit) {
        if (this.type != Type.CURRENT) {
            throw new DomainException("Only CURRENT accounts can adjust overdraft.");
        }
        if (newOverdraftLimit == null || newOverdraftLimit.isGreaterThan(Money.zero())) {
            throw new DomainException("Overdraft limit must be zero or negative.");
        }
        // recrée la policy
        this.overdraftPolicy = new FixedOverdraft(newOverdraftLimit);
    }

    /** SAVINGS uniquement. */
    public void adjustCeiling(Money newCeiling) {
        if (this.type != Type.SAVINGS) {
            throw new DomainException("Only SAVINGS accounts can adjust ceiling.");
        }
        if (newCeiling == null || newCeiling.isLessThanOrEqual(Money.zero())) {
            throw new DomainException("Ceiling must be strictly positive.");
        }
        // recrée la policy
        this.ceilingPolicy = new FixedCeiling(newCeiling);
    }

    // ---------- Historique : ordre inverse chrono ----------

    public void addOperationToOperationList(Operation operation) {
        Objects.requireNonNull(operation);
        this.operations.add(operation);
        this.operations.sort(Comparator.comparing(Operation::at).reversed());
    }

    // ---------- Getters (API lue dans les tests) ----------

    public Type getType() { return type; }
    public Money getBalance() { return balance; }
    public Money balance() { return balance; }
    public List<Operation> getOperations() { return Collections.unmodifiableList(operations); }
    public AccountId getId() { return id; }
    public Clock getClock() { return clock; }

    /**
     * Expose la valeur métier depuis la policy.
     * Retourne null si non pertinent pour le type.
     */
    public Money getOverdraft() {
        if (overdraftPolicy instanceof FixedOverdraft fo) {
            return fo.getOverdraft(); // ← nécessite un getter public dans FixedOverdraft
        }
        return null;
    }

    /**
     * Expose la valeur métier depuis la policy.
     * Retourne null si non pertinent pour le type.
     */
    public Money getCeiling() {
        if (ceilingPolicy instanceof FixedCeiling fc) {
            return fc.getCeiling(); // ← nécessite un getter public dans FixedCeiling
        }
        return null; // NoCeiling / autres implémentations
    }
}
