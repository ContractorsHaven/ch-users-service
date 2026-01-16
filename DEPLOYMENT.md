# Deployment Guide

This document covers deployment options for the ch-users-service, a Spring Boot WebFlux application with PostgreSQL.

## Prerequisites

- Java 21 (Eclipse Temurin)
- Maven 3.9+
- Docker & Docker Compose
- Kubernetes cluster (for production)
- Helm 3.x
- kubectl configured for your cluster

## Local Development

### Using Docker Compose

Build and start the application with PostgreSQL locally:

```bash
docker-compose up --build
```

This starts:
- **Application**: http://localhost:9001
- **PostgreSQL**: localhost:5432 (database: ch-users-db)

The app waits for PostgreSQL to be healthy before starting.

Run in detached mode:

```bash
docker-compose up --build -d
```

Stop the environment:

```bash
docker-compose down
```

To remove data volumes (required if changing database name or starting fresh):

```bash
docker-compose down -v
```

### Running from Source

```bash
# Build the application
./mvnw clean compile

# Run tests
./mvnw test

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Building

### Build JAR

```bash
./mvnw clean package -DskipTests
```

The JAR is created at `target/ch-users-service-<version>.jar`.

### Build Docker Image

```bash
docker build -t ch-users-service:latest .
```

The Dockerfile uses a multi-stage build:
1. Maven build stage with JDK 21
2. Runtime stage with JRE 21 (minimal image)

## Kubernetes Deployment

### Using Helm

Deploy to a Kubernetes cluster:

```bash
# Install
helm install ch-users-service ./helmchart -n contractors-haven --create-namespace

# Upgrade
helm upgrade ch-users-service ./helmchart -n contractors-haven

# Uninstall
helm uninstall ch-users-service -n contractors-haven
```

### Custom Values

Override default values:

```bash
helm install ch-users-service ./helmchart \
  --set image.tag=0.0.2 \
  --set resources.limits.memory=2048Mi \
  -n contractors-haven
```

### Key Helm Values

| Parameter | Default | Description |
|-----------|---------|-------------|
| `image.repository` | `danyzift/ch-users-service` | Docker image repository |
| `image.tag` | `0.0.1` | Image tag |
| `replicaCount` | `1` | Number of replicas |
| `resources.limits.cpu` | `500m` | CPU limit |
| `resources.limits.memory` | `1024Mi` | Memory limit |
| `resources.requests.cpu` | `250m` | CPU request |
| `resources.requests.memory` | `512Mi` | Memory request |

## ArgoCD (GitOps)

The application is configured for ArgoCD-based GitOps deployment.

### Register Application

```bash
kubectl apply -f argocd/application.yaml
```

### Sync Behavior

ArgoCD is configured with:
- **Automated sync**: Deploys changes automatically when main branch is updated
- **Self-healing**: Reverts manual cluster changes to match git state
- **Auto-pruning**: Removes resources deleted from git
- **Retry policy**: 5 attempts with exponential backoff

### Manual Sync

```bash
argocd app sync ch-users-service
```

## CI/CD Pipeline

GitHub Actions automates the entire build and deployment process.

### Triggers

- Push to `main` or `develop` branches
- Pull requests to `main`

### Pipeline Stages

1. **Build**: Compile with Maven
2. **Test**: Run unit and integration tests
3. **Version**: Auto-increment version from git tags
4. **Docker**: Build and push multi-platform image
5. **Tag**: Create git tag (main branch only)
6. **Update**: Update Helm values with new image tag

### Required Secrets

Configure these in GitHub repository settings:

| Secret | Description |
|--------|-------------|
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub password/token |

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | - | Active Spring profile |
| `SPRING_R2DBC_URL` | `r2dbc:postgresql://localhost:5432/ch-users-db` | Database URL |
| `SPRING_R2DBC_USERNAME` | `chuser` | Database username |
| `SPRING_R2DBC_PASSWORD` | `ch-password` | Database password |

### Profiles

- **default**: Production settings (port 8080)
- **dev**: Local development (port 8081, debug logging)

## Health Checks & Monitoring

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Overall health status |
| `/actuator/health/liveness` | Liveness probe |
| `/actuator/health/readiness` | Readiness probe |
| `/actuator/prometheus` | Prometheus metrics |
| `/actuator/info` | Application info |

### Kubernetes Probes

The Helm chart configures:
- **Startup probe**: 30s initial delay, checks `/actuator/health`
- **Liveness probe**: 10s interval
- **Readiness probe**: 5s interval

### Prometheus Integration

The deployment includes annotations for Prometheus scraping:

```yaml
prometheus.io/scrape: "true"
prometheus.io/port: "8080"
prometheus.io/path: "/actuator/prometheus"
```

## Database Migrations

Flyway manages database schema migrations automatically on application startup.

Migration files are located in `src/main/resources/db/migration/`.

### Naming Convention

```
V<version>__<description>.sql
```

Example: `V1__create_users_table.sql`

## Troubleshooting

### Check Application Logs

```bash
# Kubernetes
kubectl logs -f deployment/ch-users-service -n contractors-haven

# Docker Compose
docker-compose logs -f app
```

### Verify Database Connection

```bash
# Kubernetes
kubectl exec -it ch-users-service-postgresql-0 -n contractors-haven -- psql -U chuser -d ch-users-db

# Docker Compose
docker-compose exec db psql -U chuser -d ch-users-db
```

### Check ArgoCD Sync Status

```bash
argocd app get ch-users-service
```

### Force Resync

```bash
argocd app sync ch-users-service --force
```