# Guide Docker

## Démarrage

```bash
# 1. Configuration
cp .env.example .env

# 2. Lancement
docker compose up -d --build

# 3. Accès
# API : http://localhost:8080
```

## Profils Spring

- **dev** : H2 in-memory (console : `/h2`)
- **prod** : PostgreSQL (configurer `SPRING_DATASOURCE_*`)

## Commandes Courantes

```bash
# Logs
docker logs -f app

# Rebuild app
docker compose build app && docker compose up -d app

# Arrêt
docker compose down -v

# Health check DB
docker inspect --format='{{.State.Health.Status}}' bank-db
```

## CI/CD Pipeline

1. **unit-tests** → `mvn verify`
2. **build-jar** → artifact JAR
3. **docker-build-push** → GitLab Registry
4. **deploy-prod** → déploiement SSH (optionnel)

## Troubleshooting

| Problème | Solution |
|----------|----------|
| App en attente | Vérifier health check DB |
| Connexion refusée | Valider `SPRING_DATASOURCE_URL` (host: `db`) |
| Erreur schema | `ddl-auto: validate` → ajouter migrations (Flyway) |