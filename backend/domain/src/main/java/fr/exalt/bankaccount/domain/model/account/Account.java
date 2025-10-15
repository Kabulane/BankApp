package fr.exalt.bankaccount.domain.model.account;

import fr.exalt.bankaccount.domain.model.account.operation.Operation;
import fr.exalt.bankaccount.domain.model.account.rules.ceilingpolicy.CeilingPolicy;
import fr.exalt.bankaccount.domain.model.account.rules.overdraftpolicy.OverdraftPolicy;
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
                    Money overdraft,
                    Money ceiling,
                    OverdraftPolicy overdraftPolicy,
                    CeilingPolicy ceilingPolicy,
                    List<Operation> initialOperations) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.clock = (clock == null) ? Clock.systemUTC() : clock;

        this.balance = (balance == null) ? Money.zero() : balance;

        this.overdraftPolicy = Objects.requireNonNull(overdraftPolicy);
        this.ceilingPolicy = Objects.requireNonNull(ceilingPolicy);

        // Copie défensive + tri inverse chrono si fourni
        this.operations = new ArrayList<>();
        if (initialOperations != null && !initialOperations.isEmpty()) {
            this.operations.addAll(initialOperations);
            this.operations.sort(Comparator.comparing(Operation::at).reversed());
        }
    }

    // ---------------------
    // Factories d'ouverture
    // ---------------------

    public static Account openCurrent(AccountId accountId, Money of, Clock fixedClock) {
        return null;
    }
    public static Account openSavings(AccountId id, Money depositCeiling, Clock clock) {
        return null;
    }

    // ---------- Réhydratation ----------

    public static Account rehydrate(AccountId id,
                                    Type type,
                                    Money currentBalance,
                                    List<Operation> pastOperations,
                                    Money overdraftOrNull,   // CURRENT: <= 0 ; SAVINGS: null
                                    Money ceilingOrNull,     // SAVINGS: > 0 ; CURRENT: null
                                    Clock clock) {
        return null;
    }

    // ---------- Commandes ----------

    public void deposit(Money amount) {

    }

    public void withdraw(Money amount) {

    }

    /** CURRENT uniquement. */
    public void adjustOverdraftLimit(Money newOverdraftLimit) {

    }

    /** SAVINGS uniquement. */
    public void adjustCeiling(Money newCeiling) {

    }

    // ---------- Historique : ordre inverse chrono ----------

    public void addOperationToOperationList(Operation operation) {

    }

    // ---------- Getters (API lue dans tes tests) ----------

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
        return null;
    }

    /**
     * Expose la valeur métier depuis la policy.
     * Retourne null si non pertinent pour le type.
     */
    public Money getCeiling() {
        return null;
    }
}
