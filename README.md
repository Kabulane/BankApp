# Documentation Technique - Bank Account Application

## Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Structure du projet](#structure-du-projet)
4. [Technologies utilisées](#technologies-utilisées)
5. [Installation et configuration](#installation-et-configuration)
6. [Modèle de domaine](#modèle-de-domaine)
7. [API REST](#api-rest)
8. [Tests](#tests)
9. [Bonnes pratiques](#bonnes-pratiques)
10. [Contribution](#contribution)

---

## Vue d'ensemble

Bank Account est une application de gestion de comptes bancaires développée dans le cadre d'un test technique. L'application implémente les fonctionnalités essentielles de gestion de comptes courants et de comptes épargne, en respectant les principes de l'architecture hexagonale.

### Fonctionnalités principales

- Ouverture de comptes courants avec découvert autorisé
- Ouverture de comptes épargne avec plafond de dépôt
- Effectuer des dépôts
- Effectuer des retraits
- Consultation du solde et de l'historique des opérations
- Gestion des règles métier (découvert, plafond)

### Objectifs du projet

- Démontrer la maîtrise de l'architecture hexagonale
- Appliquer les principes SOLID et Domain-Driven Design
- Assurer une couverture de tests complète
- Produire du code maintenable et évolutif

---

## Architecture

### Architecture hexagonale (Ports & Adapters)

Le projet suit strictement les principes de l'architecture hexagonale, garantissant une séparation claire entre la logique métier et les préoccupations techniques.

```
┌─────────────────────────────────────────────────────────────┐
│                     INFRASTRUCTURE                          │
│  ┌────────────────┐              ┌────────────────┐        │
│  │  REST API      │              │  JPA/Hibernate │        │
│  │  Controllers   │              │  Repositories  │        │
│  └────────┬───────┘              └────────┬───────┘        │
│           │                               │                 │
│           │ Driving Adapters              │ Driven Adapters│
└───────────┼───────────────────────────────┼─────────────────┘
            │                               │
            ▼                               ▼
┌───────────────────────────────────────────────────────────┐
│                      APPLICATION                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │              Use Cases / Services                   │  │
│  │  - DepositService                                   │  │
│  │  - WithdrawService                                  │  │
│  │  - OpenAccountService                               │  │
│  └─────────┬──────────────────────────┬────────────────┘  │
│            │ Ports (in)               │ Ports (out)       │
└────────────┼──────────────────────────┼────────────────────┘
             │                          │
             ▼                          ▼
┌─────────────────────────────────────────────────────────────┐
│                        DOMAIN                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │        Core Business Logic                           │  │
│  │  - Account (Aggregate)                               │  │
│  │  - Operation (Entity)                                │  │
│  │  - Money (Value Object)                              │  │
│  │  - Business Rules & Policies                         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Avantages de cette architecture

- **Indépendance de la logique métier** : Le domaine ne dépend d'aucune technologie
- **Testabilité accrue** : Chaque couche peut être testée indépendamment
- **Flexibilité** : Remplacement facile des adapters (base de données, API, etc.)
- **Évolutivité** : Ajout de nouvelles fonctionnalités sans impacter le cœur métier

---

## Structure du projet

Le projet est organisé en modules Maven distincts, chacun ayant une responsabilité claire :

```
corentin-lamblin-bank-account-v2/
│
├── pom.xml                          # POM parent (gestion globale)
├── README.md
├── assets/                          # Ressources graphiques
│   └── hexa-schema.png
│
└── backend/
    ├── pom.xml                      # POM du backend
    │
    ├── domain/                      # Couche Domaine
    │   ├── pom.xml
    │   └── src/
    │       ├── main/java/
    │       │   └── fr/exalt/bankaccount/domain/
    │       │       ├── model/
    │       │       │   ├── account/
    │       │       │   │   ├── Account.java
    │       │       │   │   ├── AccountId.java
    │       │       │   │   ├── operation/
    │       │       │   │   │   ├── Operation.java
    │       │       │   │   │   └── OperationId.java
    │       │       │   │   └── rules/
    │       │       │   │       ├── ceilingpolicy/
    │       │       │   │       │   ├── CeilingPolicy.java
    │       │       │   │       │   ├── FixedCeiling.java
    │       │       │   │       │   └── NoCeiling.java
    │       │       │   │       └── overdraftpolicy/
    │       │       │   │           ├── OverdraftPolicy.java
    │       │       │   │           ├── FixedOverdraft.java
    │       │       │   │           └── NoOverdraft.java
    │       │       │   ├── money/
    │       │       │   │   └── Money.java
    │       │       │   └── exception/
    │       │       │       ├── DomainException.java
    │       │       │       ├── BusinessRuleViolationException.java
    │       │       │       ├── InvariantViolationException.java
    │       │       │       ├── InsufficientFundsException.java
    │       │       │       └── CeilingExceededException.java
    │       │       └── port/          # Interfaces des ports
    │       │
    │       └── test/java/             # Tests unitaires du domaine
    │
    ├── application/                   # Couche Application
    │   ├── pom.xml
    │   └── src/
    │       ├── main/java/
    │       │   └── fr/exalt/bankaccount/application/
    │       │       ├── port/
    │       │       │   ├── in/        # Use Cases (ports entrants)
    │       │       │   │   ├── DepositUseCase.java
    │       │       │   │   ├── WithdrawUseCase.java
    │       │       │   │   └── OpenAccountUseCase.java
    │       │       │   └── out/       # Repositories (ports sortants)
    │       │       │       ├── AccountRepository.java
    │       │       │       └── OperationRepository.java
    │       │       ├── service/
    │       │       │   └── account/
    │       │       │       ├── DepositService.java
    │       │       │       ├── WithdrawService.java
    │       │       │       ├── OpenCurrentAccountService.java
    │       │       │       └── OpenSavingsAccountService.java
    │       │       ├── dto/            # Data Transfer Objects
    │       │       │   └── account/
    │       │       │       ├── operation/
    │       │       │       └── query/
    │       │       └── exception/      # Exceptions applicatives
    │       │
    │       └── test/java/              # Tests des services
    │
    ├── infrastructure/                 # Couche Infrastructure
    │   ├── pom.xml
    │   └── src/
    │       ├── main/
    │       │   ├── java/
    │       │   │   └── fr/exalt/bankaccount/infrastructure/
    │       │   │       ├── jpa/
    │       │   │       │   ├── adapter/
    │       │   │       │   │   ├── AccountRepositoryAdapter.java
    │       │   │       │   │   └── OperationRepositoryAdapter.java
    │       │   │       │   ├── entity/
    │       │   │       │   │   ├── AccountEntity.java
    │       │   │       │   │   └── OperationEntity.java
    │       │   │       │   ├── mapper/
    │       │   │       │   │   ├── AccountMapper.java
    │       │   │       │   │   └── OperationMapper.java
    │       │   │       │   └── spring/
    │       │   │       │       ├── AccountJpaRepository.java
    │       │   │       │       └── OperationJpaRepository.java
    │       │   │       ├── rest/
    │       │   │       │   ├── controller/
    │       │   │       │   │   ├── AccountController.java
    │       │   │       │   │   └── OperationController.java
    │       │   │       │   ├── dto/
    │       │   │       │   │   ├── request/
    │       │   │       │   │   └── response/
    │       │   │       │   └── exception/
    │       │   │       │       └── RestExceptionHandler.java
    │       │   │       └── config/
    │       │   │           └── ApplicationServiceConfig.java
    │       │   │
    │       │   └── resources/
    │       │       ├── application.yml
    │       │       └── application-dev.yml
    │       │
    │       └── test/                   # Tests d'intégration
    │
    └── boot/                           # Module de démarrage
        ├── pom.xml
        └── src/
            └── main/java/
                └── fr/exalt/bankaccount/boot/
                    └── BankAccountApplication.java
```

### Description des modules

#### domain/
Contient la logique métier pure, sans dépendance externe. C'est le cœur de l'application.

**Responsabilités :**
- Définition des entités et agrégats métier
- Implémentation des règles métier
- Gestion des invariants
- Définition des exceptions métier

**Dépendances :** Aucune (module totalement autonome)

#### application/
Orchestre les cas d'usage en utilisant la logique du domaine.

**Responsabilités :**
- Définition des ports (interfaces)
- Implémentation des use cases
- Coordination des opérations métier
- Gestion des transactions
- Transformation des données (DTOs)

**Dépendances :** domain

#### infrastructure/
Implémente les adapters techniques.

**Responsabilités :**
- Persistence des données (JPA/Hibernate)
- Exposition de l'API REST
- Configuration Spring Boot
- Mappage entités <-> modèle de domaine

**Dépendances :** domain, application

#### boot/
Point d'entrée de l'application Spring Boot.

**Responsabilités :**
- Configuration de démarrage
- Assemblage des dépendances
- Initialisation du contexte Spring

**Dépendances :** domain, application, infrastructure

---

## Technologies utilisées

### Backend

| Technologie | Version | Usage |
|------------|---------|-------|
| Java | 17 | Langage de programmation |
| Spring Boot | 3.3.4 | Framework d'application |
| Spring Data JPA | 3.3.4 | Abstraction de persistence |
| Hibernate | 6.x | ORM |
| Maven | 3.9+ | Gestion de build et dépendances |
| H2 Database | - | Base de données en mémoire (dev/test) |
| JUnit 5 | 5.10+ | Tests unitaires |
| Mockito | 5.x | Mocking pour les tests |
| AssertJ | 3.x | Assertions fluides pour les tests |

### Outils de développement

- **Java 17** : Version LTS avec records, pattern matching
- **Maven Multi-Module** : Organisation modulaire du projet
- **Lombok** (optionnel) : Réduction du code boilerplate

### Standards et conventions

- **Google Java Style Guide** : Convention de code
- **Conventional Commits** : Format des messages de commit
- **Architecture Hexagonale** : Pattern d'architecture
- **DDD** : Domain-Driven Design

---

## Installation et configuration

### Prérequis

Avant de démarrer, assurez-vous d'avoir installé :

- **JDK 17 ou supérieur**
  ```bash
  java -version
  # Devrait afficher : java version "17.x.x" ou supérieur
  ```

- **Maven 3.9 ou supérieur**
  ```bash
  mvn -version
  # Devrait afficher : Apache Maven 3.9.x ou supérieur
  ```

- **Git** (pour cloner le repository)

### Installation

#### 1. Cloner le projet

```bash
git clone https://gitlab.com/exalt-it-dojo/candidats/corentin-lamblin-bank-account.git
cd corentin-lamblin-bank-account
```

#### 2. Compiler le projet

```bash
# Compilation et installation dans le repository local
mvn clean install
```

Cette commande va :
- Compiler tous les modules
- Exécuter les tests unitaires et d'intégration
- Créer les artifacts JAR

#### 3. Lancer l'application

```bash
# Démarrer le serveur Spring Boot
mvn spring-boot:run -pl backend/boot
```

L'application démarre sur le port **8080** par défaut.

#### 4. Vérifier le démarrage

```bash
# Tester que l'application répond
curl http://localhost:8080/actuator/health
```

### Configuration

#### Profils Spring

L'application supporte plusieurs profils :

- **default** : Configuration minimale
- **dev** : Configuration de développement (console H2, logs détaillés)
- **test** : Configuration pour les tests

Pour activer un profil :

```bash
# Via ligne de commande
mvn spring-boot:run -pl backend/boot -Dspring-boot.run.profiles=dev

# Via variable d'environnement
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run -pl backend/boot
```

#### Configuration de la base de données

**Mode développement (H2)** :

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:bankaccount
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
```

Console H2 accessible sur : `http://localhost:8080/h2-console`

---

## Modèle de domaine

Le modèle de domaine est le cœur de l'application. Il encapsule toute la logique métier et les règles business.

### Concepts clés

#### Account (Agrégat)

L'agrégat `Account` représente un compte bancaire. C'est la racine d'agrégat dans notre modèle DDD.

**Types de comptes :**

```java
public enum Type {
    CURRENT,   // Compte courant (avec découvert autorisé)
    SAVINGS    // Compte épargne (avec plafond de dépôt)
}
```

**Règles métier :**

**Compte Courant (CURRENT) :**
- Possède un découvert autorisé (valeur négative ou zéro)
- Pas de plafond de dépôt
- Les retraits sont autorisés jusqu'à la limite du découvert
- Policy : `FixedOverdraft` + `NoCeiling`

**Compte Épargne (SAVINGS) :**
- Pas de découvert autorisé
- Possède un plafond de dépôt (valeur strictement positive)
- Les retraits ne peuvent pas rendre le solde négatif
- Policy : `NoOverdraft` + `FixedCeiling`

**Invariants :**

```java
// Invariants toujours respectés
- AccountId non null
- Type non null
- Balance initialisée
- Policies configurées selon le type
```

**Opérations métier :**

```java
// Ouverture d'un compte courant
Account account = Account.openCurrent(Money.of(-500), Clock.systemUTC());

// Ouverture d'un compte épargne
Account account = Account.openSavings(Money.of(10000), Clock.systemUTC());

// Dépôt
Operation deposit = account.deposit(Money.of(100));

// Retrait
Operation withdrawal = account.withdraw(Money.of(50));

// Ajustement du découvert (CURRENT uniquement)
account.adjustOverdraftLimit(Money.of(-1000));

// Ajustement du plafond (SAVINGS uniquement)
account.adjustCeiling(Money.of(15000));
```

#### Operation (Entité)

Représente une opération bancaire (dépôt ou retrait).

```java
public class Operation {
    public enum Type {
        DEPOSIT,     // Crédit
        WITHDRAWAL   // Débit
    }
    
    private final OperationId id;
    private final AccountId accountId;
    private final Money amount;
    private final Type type;
    private final LocalDateTime timestamp;
}
```

**Règles métier :**
- Les montants doivent être strictement positifs
- Chaque opération est horodatée
- Une opération est immuable une fois créée

#### Money (Value Object)

Représente une somme d'argent de manière type-safe.

```java
Money amount = Money.of(100.50);
Money sum = amount.add(Money.of(50));
Money difference = amount.subtract(Money.of(30));

// Comparaisons
boolean isGreater = amount.isGreaterThan(Money.of(50));
boolean isPositive = amount.isGreaterThan(Money.zero());
```

**Caractéristiques :**
- Immuable
- Gestion de la précision décimale
- Opérations arithmétiques sûres
- Comparaisons type-safe

### Patterns de domaine

#### Strategy Pattern : Policies

Les règles variables selon le type de compte sont encapsulées dans des policies interchangeables :

**OverdraftPolicy :**
```java
public interface OverdraftPolicy {
    void validateWithdraw(Money currentBalance, Money amount);
}

// Implémentations
- NoOverdraft : Interdit tout découvert
- FixedOverdraft : Autorise un découvert jusqu'à une limite
```

**CeilingPolicy :**
```java
public interface CeilingPolicy {
    void validateDeposit(Money currentBalance, Money amount);
}

// Implémentations
- NoCeiling : Pas de limitation
- FixedCeiling : Plafond fixe
```

#### Factory Pattern : Création de comptes

```java
// Factories statiques dans Account
Account current = Account.openCurrent(overdraft, clock);
Account savings = Account.openSavings(ceiling, clock);

// Réhydratation depuis la persistence
Account rehydrated = Account.rehydrate(
    id, type, balance, overdraft, ceiling, clock
);
```

### Exceptions métier

Hiérarchie des exceptions :

```
DomainException (abstract)
├── InvariantViolationException
│   └── Ex: "AccountId cannot be null"
└── BusinessRuleViolationException
    ├── InsufficientFundsException
    │   └── Ex: "Insufficient funds for withdrawal"
    └── CeilingExceededException
        └── Ex: "Deposit would exceed ceiling limit"
```

**Usage :**

```java
// Les exceptions métier sont levées par le domaine
try {
    account.withdraw(Money.of("1000"));
} catch (InsufficientFundsException e) {
    // Géré par la couche application ou infrastructure
}
```

---

## API REST

L'API REST expose les fonctionnalités de l'application via des endpoints HTTP.

### Base URL

```
http://localhost:8080/
```

### Endpoints

#### Gestion des comptes

##### Ouvrir un compte courant

```http
POST /accounts/current
Content-Type: application/json

{
  "overdraft": -500.00
}
```

**Réponse :**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

##### Ouvrir un compte épargne

```http
POST /api/accounts/savings
Content-Type: application/json

{
  "ceiling": 10000.00
}
```

**Réponse :**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001"
}
```

#### Opérations bancaires

##### Effectuer un dépôt

```http
POST /accounts/{id}/deposit
Content-Type: application/json

{
  "amount": 100.00
}
```

**Réponse :**
```json
{
  "accountId": "915f9887-d84a-40b6-b9e6-adf8044747c2",
  "newBalance": 200.00,
  "operation": {
    "id": "aad3131a-c5d0-48d5-b218-85b8f3be664a",
    "type": "DEPOSIT",
    "amount": 200.00,
    "at": "2025-10-24T00:32:02.872682200Z",
    "label":"Deposit"
  }
}
```

##### Effectuer un retrait

```http
POST /accounts/{id}/withdraw
Content-Type: application/json

{
  "amount": 50.00
}
```

**Réponse :**
```json
{
  "accountId": "915f9887-d84a-40b6-b9e6-adf8044747c2",
  "newBalance": 200.00,
  "operation": {
    "id": "aad3131a-c5d0-48d5-b218-85b8f3be664a",
    "type": "WITHDRAWAL",
    "amount": 100.00,
    "at": "2025-10-24T00:32:02.872682200Z",
    "label":"Withdrawal"
  }
}
```

##### Consulter l'historique des opérations

```http
GET /accounts/{id}/operations
```

**Réponse :**
```json
[
  {
    "id": "baa442ae-c04d-4301-bcf6-ea7d3769c821",
    "type": "DEPOSIT",
    "amount": 200.00,
    "at": "2025-10-24T00:36:32.217090Z",
    "label": "Deposit"
  },
  {
    "id": "aad3131a-c5d0-48d5-b218-85b8f3be664a",
    "type": "DEPOSIT",
    "amount": 200.00,
    "at": "2025-10-24T00:32:02.872682Z",
    "label": "Deposit"
  }
]
```

### Codes de statut HTTP

| Code | Signification | Usage |
|------|---------------|-------|
| 200  | OK | Requête réussie (GET) |
| 201  | Created | Ressource créée (POST) |
| 400  | Bad Request | Données invalides |
| 404  | Not Found | Ressource non trouvée |
| 422  | Conflict | Violation de règle métier |
| 500  | Internal Server Error | Erreur serveur |

### TODO Gestion des erreurs 

Format standard des erreurs :

```json
{
  "timestamp": "2025-10-24T11:00:00Z",
  "status": 409,
  "error": "Business Rule Violation",
  "message": "Insufficient funds for withdrawal. Current balance: 100.00, Attempted withdrawal: 200.00",
  "path": "/api/operations/withdraw"
}
```

### Exemples avec cURL

```bash
# Ouvrir un compte courant
curl -X POST http://localhost:8080/accounts/current \
  -H "Content-Type: application/json" \
  -d '{"overdraft": -500.00}'

# Effectuer un dépôt
curl -X POST http://localhost:8080/accounts/{id}/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00
  }'

# Consulter l'historique
curl http://localhost:8080/accounts/550e8400-e29b-41d4-a716-446655440000/operations
```

---

## Tests

La couverture de tests est un élément essentiel du projet. L'objectif est d'atteindre au minimum 80% de couverture sur les couches domain et application.

### Stratégie de tests

```
┌─────────────────────────────────────────────┐
│         Tests End-to-End (E2E)              │ ← Peu nombreux
│      (Via API REST complète)                │
└─────────────────────────────────────────────┘
                    ▲
┌─────────────────────────────────────────────┐
│         Tests d'Intégration                 │ ← Plus nombreux
│  (Spring Boot Test, TestContainers)         │
└─────────────────────────────────────────────┘
                    ▲
┌─────────────────────────────────────────────┐
│           Tests Unitaires                   │ ← Très nombreux
│        (JUnit 5 + Mockito)                  │
└─────────────────────────────────────────────┘
```

### Tests unitaires du domaine

Les tests du domaine vérifient la logique métier pure, sans dépendance externe.

**Exemple :** Test de l'entité `Account`

```java
@Test
void should_deposit_money_successfully() {
    // Given
    Account account = Account.openCurrent(
        Money.of("-500"), 
        Clock.systemUTC()
    );
    Money depositAmount = Money.of("100");
    
    // When
    Operation operation = account.deposit(depositAmount);
    
    // Then
    assertThat(account.getBalance())
        .isEqualTo(Money.of("100"));
    assertThat(operation.getType())
        .isEqualTo(Operation.Type.DEPOSIT);
    assertThat(operation.getAmount())
        .isEqualTo(depositAmount);
}

@Test
void should_throw_exception_when_deposit_amount_is_negative() {
    // Given
    Account account = Account.openCurrent(
        Money.of("-500"), 
        Clock.systemUTC()
    );
    
    // When & Then
    assertThatThrownBy(() -> account.deposit(Money.of("-100")))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("must be strictly positive");
}

@Test
void should_allow_withdrawal_within_overdraft_limit() {
    // Given
    Account account = Account.openCurrent(
        Money.of("-500"), 
        Clock.systemUTC()
    );
    account.deposit(Money.of("200"));
    
    // When
    Operation operation = account.withdraw(Money.of("600"));
    
    // Then
    assertThat(account.getBalance())
        .isEqualTo(Money.of("400"));
}

@Test
void should_reject_withdrawal_exceeding_overdraft() {
    // Given
    Account account = Account.openCurrent(
        Money.of("-500"), 
        Clock.systemUTC()
    );
    
    // When & Then
    assertThatThrownBy(() -> account.withdraw(Money.of("600")))
        .isInstanceOf(InsufficientFundsException.class);
}
```

**Localisation :** `backend/domain/src/test/java/`

### Tests unitaires des services

Les tests des services vérifient l'orchestration des use cases.

**Exemple :** Test de `DepositService`

```java
@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private OperationRepository operationRepository;
    
    @InjectMocks
    private DepositService depositService;

    @Test
    void should_execute_deposit_successfully() {
        // Given
        AccountId accountId = AccountId.newId();
        Account account = Account.openCurrent(
            Money.of("-500"), 
            Clock.systemUTC()
        );
        DepositCommand command = new DepositCommand(
            accountId, 
            Money.of("100")
        );
        
        when(accountRepository.findById(accountId))
            .thenReturn(account);
        
        // When
        DepositResult result = depositService.handle(command);
        
        // Then
        assertThat(result.newBalance())
            .isEqualTo(Money.of("100"));
        verify(accountRepository).save(account);
        verify(operationRepository).save(any(Operation.class));
    }
    
    @Test
    void should_throw_exception_when_account_not_found() {
        // Given
        AccountId unknownId = AccountId.newId();
        DepositCommand command = new DepositCommand(
            unknownId, 
            Money.of("100")
        );
        
        when(accountRepository.findById(unknownId))
            .thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> depositService.handle(command))
            .isInstanceOf(AccountNotFoundApplicationException.class);
    }
}
```

**Localisation :** `backend/application/src/test/java/`

### Tests d'intégration

Les tests d'intégration vérifient le comportement de l'application avec une vraie base de données.

**Exemple :** Test d'intégration du repository

```java
@SpringBootTest
@ActiveProfiles("test")
class AccountRepositoryAdapterIT {

    @Autowired
    private AccountRepositoryAdapter accountRepository;
    
    @Test
    void should_save_and_retrieve_account() {
        // Given
        Account account = Account.openCurrent(
            Money.of("-500"), 
            Clock.systemUTC()
        );
        
        // When
        accountRepository.save(account);
        Account retrieved = accountRepository.findById(account.getId());
        
        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(account.getId());
        assertThat(retrieved.getType()).isEqualTo(Account.Type.CURRENT);
        assertThat(retrieved.getBalance()).isEqualTo(Money.zero());
        assertThat(retrieved.getOverdraft()).isEqualTo(Money.of("-500"));
    }
}
```

**Localisation :** `backend/infrastructure/src/test/java/`

### Tests des contrôleurs REST

Les tests des contrôleurs vérifient les endpoints HTTP.

```java
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private OpenCurrentAccountService openCurrentAccountService;

    @Test
    void should_open_current_account_via_rest_api() throws Exception {
        // Given
        OpenCurrentAccountCommand command = 
            new OpenCurrentAccountCommand(Money.of("-500"));
        Account account = Account.openCurrent(
            Money.of("-500"), 
            Clock.systemUTC()
        );
        
        when(openCurrentAccountService.handle(any()))
            .thenReturn(new OpenCurrentAccountResult(account));
        
        // When & Then
        mockMvc.perform(post("/api/accounts/current")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"overdraftLimit\": -500.00}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accountId").exists())
            .andExpect(jsonPath("$.type").value("CURRENT"))
            .andExpect(jsonPath("$.balance").value("0.00"))
            .andExpect(jsonPath("$.overdraftLimit").value("-500.00"));
    }
}
```

**Localisation :** `backend/infrastructure/src/test/java/`

### Exécution des tests

```bash
# Tous les tests
mvn test

# Tests d'un module spécifique
mvn test -pl backend/domain

# Tests d'une classe spécifique
mvn test -Dtest=AccountTest

# Tests avec rapport de couverture
mvn test jacoco:report

# Tests d'intégration uniquement
mvn verify -DskipUTs=true
```

### Rapport de couverture

Les rapports de couverture sont générés par JaCoCo :

```bash
# Générer le rapport
mvn clean test jacoco:report

# Consulter le rapport
open backend/domain/target/site/jacoco/index.html
```

**Objectif de couverture :**
- Domain : ≥ 80%
- Application : ≥ 80%
- Infrastructure : ≥ 60% (code technique, moins critique)

---

## Bonnes pratiques

### Principes SOLID

Le projet respecte les cinq principes SOLID :

#### S - Single Responsibility Principle
Chaque classe a une seule responsabilité :
- `Account` : Gestion d'un compte bancaire
- `DepositService` : Orchestration du dépôt
- `AccountRepositoryAdapter` : Persistence des comptes

#### O - Open/Closed Principle
Extensible sans modification :
- Policies (OverdraftPolicy, CeilingPolicy) : ajout de nouvelles implémentations sans modifier l'existant
- Strategy Pattern pour les règles métier

#### L - Liskov Substitution Principle
Les implémentations peuvent être substituées :
```java
OverdraftPolicy policy = new FixedOverdraft(Money.of(-500));
// Remplaçable par
OverdraftPolicy policy = new NoOverdraft();
```

#### I - Interface Segregation Principle
Interfaces spécifiques et ciblées :
- `AccountRepository` : opérations CRUD sur les comptes
- `DepositUseCase` : cas d'usage de dépôt uniquement

#### D - Dependency Inversion Principle
Dépendance sur des abstractions :
```java
// Le service dépend d'une interface, pas d'une implémentation
public class DepositService {
    private final AccountRepository repository; // Interface
}
```

### Domain-Driven Design (DDD)

#### Agrégats
- `Account` est la racine d'agrégat
- `Operation` est une entité interne à l'agrégat
- Les modifications passent toujours par la racine

#### Value Objects
- `Money` : objet-valeur immuable
- `AccountId`, `OperationId` : identifiants typés

#### Ubiquitous Language
Le code reflète le langage métier :
- `deposit()`, `withdraw()` : terminologie bancaire
- `overdraft`, `ceiling` : concepts métier clairs

#### Bounded Context
Le domaine bancaire est isolé dans son propre contexte, sans fuite vers l'infrastructure.

### Clean Code

#### Nommage explicite
```java
// ✅ Bon
public Operation deposit(Money amount)

// ❌ Mauvais
public Operation op(Money m)
```

#### Méthodes courtes
```java
// ✅ Bon : une méthode fait une chose
public Operation deposit(Money amount) {
    validateDepositAmount(amount);
    ceilingPolicy.validateDeposit(this.balance, amount);
    Operation op = Operation.of(this.id, amount, Operation.Type.DEPOSIT);
    this.balance = op.applyTo(this.balance);
    return op;
}
```

#### Gestion des erreurs
```java
// ✅ Bon : exceptions métier explicites
if (amount.isLessThanOrEqual(Money.zero())) {
    throw new BusinessRuleViolationException(
        "Deposit amount must be strictly positive"
    );
}
```

### Standards de commit

Le projet utilise Conventional Commits :

```bash
# Format
<type>(<scope>): <description>

# Exemples
feat(domain): add support for savings account ceiling
fix(application): correct balance calculation in DepositService
test(domain): add tests for overdraft policy
docs(readme): update installation instructions
refactor(infrastructure): simplify account mapper
```

**Types :**
- `feat` : Nouvelle fonctionnalité
- `fix` : Correction de bug
- `test` : Ajout/modification de tests
- `refactor` : Refactoring sans changement fonctionnel
- `docs` : Documentation
- `chore` : Tâches techniques (dépendances, configuration)

### Gestion des branches

**Modèle de branchement :**

```
main
  └── corentin/dev
       ├── feature/account-creation
       ├── feature/operations
       ├── fix/deposit-validation
       └── chore/update-dependencies
```

**Convention de nommage :**
- `feature/nom-fonctionnalite` : Nouvelle fonctionnalité
- `fix/description-bug` : Correction de bug
- `chore/description` : Maintenance technique

**Workflow :**
1. Créer une branche depuis `corentin/dev`
2. Développer et tester
3. Commit avec message conventional
4. Merge request vers `corentin/dev`

---

## Contribution

### Guide pour contribuer

#### 1. Forker et cloner le projet

```bash
git clone https://gitlab.com/exalt-it-dojo/candidats/corentin-lamblin-bank-account.git
cd corentin-lamblin-bank-account
```

#### 2. Créer une branche de travail

```bash
git checkout corentin/dev
git pull origin corentin/dev
git checkout -b feature/ma-nouvelle-fonctionnalite
```

#### 3. Développer

- Écrire les tests en premier (TDD)
- Implémenter la fonctionnalité
- S'assurer que tous les tests passent
- Respecter les conventions de code

#### 4. Commiter

```bash
# Ajouter les fichiers modifiés
git add .

# Commiter avec un message conventional
git commit -m "feat(domain): add support for joint accounts"
```

#### 5. Pousser et créer une Merge Request

```bash
git push origin feature/ma-nouvelle-fonctionnalite
```

Puis créer une Merge Request sur GitLab.

### Checklist avant PR

- [X] Les tests unitaires passent (`mvn test`)
- [X] Les tests d'intégration passent (`mvn verify`)
- [X] La couverture de code est maintenue (≥ 80%)
- [X] Le code respecte le Google Java Style Guide
- [X] La documentation est à jour
- [X] Les messages de commit suivent Conventional Commits
- [X] Pas de code commenté inutile
- [ ] Pas de dépendances inutiles ajoutées

### Code Review

Les critères de revue de code incluent :

**Architecture :**
- Respect de l'architecture hexagonale
- Séparation claire des responsabilités
- Pas de fuite de domaine vers l'infrastructure

**Qualité :**
- Tests pertinents et suffisants
- Gestion appropriée des erreurs
- Code lisible et maintenable

**Performances :**
- Pas de requêtes N+1
- Utilisation appropriée des transactions
- Pas de code dupliqué

---

## Annexes

### Glossaire

| Terme | Définition |
|-------|------------|
| **Agrégat** | Cluster d'objets traités comme une unité pour les modifications de données |
| **Architecture Hexagonale** | Pattern d'architecture isolant la logique métier des détails techniques |
| **DDD** | Domain-Driven Design, approche de conception centrée sur le domaine métier |
| **Entité** | Objet avec une identité unique qui persiste dans le temps |
| **Objet-Valeur** | Objet immuable défini par ses attributs, sans identité propre |
| **Port** | Interface définissant un point d'entrée ou de sortie du domaine |
| **Adapter** | Implémentation technique d'un port |
| **Use Case** | Cas d'usage métier orchestré par la couche application |

### Ressources complémentaires

**Architecture :**
- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

**Domain-Driven Design :**
- [Domain-Driven Design Reference - Eric Evans](https://www.domainlanguage.com/ddd/reference/)
- [Implementing Domain-Driven Design - Vaughn Vernon](https://vaughnvernon.co/?page_id=168)

**Spring Boot :**
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

**Tests :**
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### Support

Pour toute question ou problème :

**Contact :**
- Email : vaitea.lamblin@gmail.com
- GitLab : [@corentin-lamblin](https://gitlab.com/corentin-lamblin)


---

**Version de la documentation :** 1.0.0  
**Dernière mise à jour :** 24 octobre 2025