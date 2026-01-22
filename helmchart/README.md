# CH Users Service Helm Chart

Helm chart for deploying the ContractorsHaven Users Service to Kubernetes.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- PV provisioner support in the cluster (for PostgreSQL persistence)

## Installation

### Add the chart (if hosted in a repository)

```bash
helm repo add contractorshaven <repository-url>
helm repo update
```

### Install from local directory

```bash
# From the repository root
helm install ch-users-service ./helmchart

# With a custom namespace
helm install ch-users-service ./helmchart -n my-namespace --create-namespace

# With custom values
helm install ch-users-service ./helmchart -f my-values.yaml
```

### Install with custom values inline

```bash
helm install ch-users-service ./helmchart \
  --set postgresql.auth.password=my-secure-password \
  --set image.tag=0.0.6
```

## Configuration

The following table lists the configurable parameters and their default values.

### Application

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `image.registry` | Image registry | `docker.io` |
| `image.repository` | Image repository | `danyzift/ch-users-service` |
| `image.tag` | Image tag | `0.0.6` |
| `image.pullPolicy` | Image pull policy | `Always` |
| `nameOverride` | Override chart name | `""` |
| `fullnameOverride` | Override full name | `""` |

### Service

| Parameter | Description | Default |
|-----------|-------------|---------|
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.port` | Service port | `8080` |

### Resources

| Parameter | Description | Default |
|-----------|-------------|---------|
| `resources.requests.memory` | Memory request | `512Mi` |
| `resources.requests.cpu` | CPU request | `250m` |
| `resources.limits.memory` | Memory limit | `1024Mi` |
| `resources.limits.cpu` | CPU limit | `500m` |

### PostgreSQL

| Parameter | Description | Default |
|-----------|-------------|---------|
| `postgresql.enabled` | Enable bundled PostgreSQL | `true` |
| `postgresql.image.repository` | PostgreSQL image | `postgres` |
| `postgresql.image.tag` | PostgreSQL version | `18.1-alpine` |
| `postgresql.auth.database` | Database name | `CH-UsersDB` |
| `postgresql.auth.username` | Database username | `chuser` |
| `postgresql.auth.password` | Database password | `ch-password` |
| `postgresql.persistence.enabled` | Enable persistence | `true` |
| `postgresql.persistence.size` | PVC size | `5Gi` |

### OpenTelemetry

| Parameter | Description | Default |
|-----------|-------------|---------|
| `otel.enabled` | Enable OpenTelemetry | `true` |
| `otel.serviceName` | Service name for traces | `ch-users-service` |
| `otel.instrumentations.*` | Enable/disable specific instrumentations | See values.yaml |

### Health Probes

| Parameter | Description | Default |
|-----------|-------------|---------|
| `startupProbe.initialDelaySeconds` | Startup probe initial delay | `45` |
| `startupProbe.periodSeconds` | Startup probe period | `10` |
| `startupProbe.failureThreshold` | Startup probe failure threshold | `30` |
| `livenessProbe.periodSeconds` | Liveness probe period | `10` |
| `readinessProbe.periodSeconds` | Readiness probe period | `5` |

### Pod Annotations

Default annotations enable Prometheus scraping and OpenTelemetry auto-instrumentation:

```yaml
podAnnotations:
  prometheus.io/scrape: "true"
  prometheus.io/port: "8080"
  prometheus.io/path: "/actuator/prometheus"
  instrumentation.opentelemetry.io/inject-java: "true"
```

## Using an External Database

To use an external PostgreSQL database instead of the bundled one:

1. Disable the bundled PostgreSQL
2. Configure environment variables via `extraEnv` or create your own values file

```bash
helm install ch-users-service ./helmchart \
  --set postgresql.enabled=false \
  --set-string extraEnv[0].name=SPRING_R2DBC_URL \
  --set-string extraEnv[0].value="r2dbc:postgresql://external-db:5432/mydb" \
  --set-string extraEnv[1].name=SPRING_R2DBC_USERNAME \
  --set-string extraEnv[1].value="myuser" \
  --set-string extraEnv[2].name=SPRING_R2DBC_PASSWORD \
  --set-string extraEnv[2].value="mypassword"
```

## Upgrading

```bash
helm upgrade ch-users-service ./helmchart -f my-values.yaml
```

## Uninstalling

```bash
helm uninstall ch-users-service

# If you want to also delete the PVC (database data)
kubectl delete pvc -l app.kubernetes.io/instance=ch-users-service
```

## Endpoints

Once deployed, the service exposes:

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/health/liveness` | Liveness probe |
| `/actuator/health/readiness` | Readiness probe |
| `/actuator/prometheus` | Prometheus metrics |

## Accessing the Service

```bash
# Port forward for local access
kubectl port-forward svc/ch-users-service 8080:8080

# Or get the service URL (if using LoadBalancer/Ingress)
kubectl get svc ch-users-service
```

## Troubleshooting

### Check pod status
```bash
kubectl get pods -l app.kubernetes.io/name=ch-users-service
```

### View logs
```bash
kubectl logs -l app.kubernetes.io/name=ch-users-service -f
```

### Check PostgreSQL
```bash
kubectl logs -l app.kubernetes.io/name=ch-users-service,app.kubernetes.io/component=postgresql -f
```

### Describe pod for events
```bash
kubectl describe pod -l app.kubernetes.io/name=ch-users-service
```
