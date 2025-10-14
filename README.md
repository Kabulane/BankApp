# 🧩 Corentin Lamblin Bank Account — Architecture Hexagonale (Maven Multi-Module)

## 🏗️ Structure du projet

Le projet suit une architecture **hexagonale (Ports & Adapters)** et est organisé en plusieurs modules Maven pour favoriser la séparation des responsabilités et la testabilité.

```
corentin-lamblin-bank-account-v2/
├── pom.xml                  # POM parent (gestion des dépendances, build global)
├── README.md
├── backend/
│   ├── pom.xml              # POM du backend Spring Boot
│   ├── application/         # Cas d’usage (services, orchestration)
│   ├── domain/              # Modèle métier (entités, agrégats, ports)
│   ├── infrastructure/      # Adapters techniques (base de données, REST, etc.)
│   └── configuration/       # Configuration Spring Boot / injection de dépendances
└── ...                      # Autres modules (shared-kernel, front, etc.)
```

Chaque module est indépendant et communique via des **interfaces (ports)** définies dans le domaine.

---

## ⚙️ Prérequis

- **Java 17+**
- **Maven 3.9+**
- **Spring Boot 3+**
- (Optionnel) Docker pour la base de données ou services externes

---

## 🚀 Lancer le projet

À la racine du projet :

```bash
mvn clean install
```

Puis exécuter le backend (dans le module concerné) :

```bash
mvn spring-boot:run -pl backend
```

---

## 🧪 Tests

Lancer tous les tests :

```bash
mvn test
```

Pour un module spécifique :

```bash
mvn test -pl backend
```

---

## 🧱 Convention de code

- **Langage** : Java 17  
- **Style** : [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)  
- **Tests** : JUnit 5 + Mockito  
- **Couverture minimale** : 80% sur le domaine et l’application  
- **Nom des branches** :  
  - `feature/nom-fonctionnalite`
  - `fix/description-bug`
  - `chore/maintenance`

---

## 🤝 Contribution

1. Créer une branche à partir de `corentin/dev`
2. Commit clair et concis (`feat(module):`, `fix(module):`, `test(module):`, `docs(module):`…)


---

## 📂 Ressources complémentaires

- [Guide d’architecture hexagonale — Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Documentation Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Documentation Maven](https://maven.apache.org/guides/)

---

## 🧑‍💻 Équipe

| Rôle | Nom | Contact |
|------|------|----------|
| Développeur principal | Corentin Lamblin | corentin.lamblin@example.com |

---

## 📄 Licence

Ce projet est sous licence MIT — voir le fichier [LICENSE](LICENSE) pour plus de détails.
