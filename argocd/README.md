# ArgoCD Application Deployment

This directory contains the ArgoCD Application manifest for deploying ch-users-service.

## Prerequisites

1. **ArgoCD installed** on your cluster
   ```bash
   kubectl get pods -n argocd
   ```

2. **ArgoCD project exists** - The application references `project: contractors-haven`
   ```bash
   kubectl get appproject contractors-haven -n argocd
   ```

   If the project doesn't exist, create it:
   ```bash
   kubectl apply -f - <<EOF
   apiVersion: argoproj.io/v1alpha1
   kind: AppProject
   metadata:
     name: contractors-haven
     namespace: argocd
   spec:
     description: ContractorsHaven services
     sourceRepos:
       - 'https://github.com/ContractorsHaven/*'
     destinations:
       - namespace: contractors-haven
         server: https://kubernetes.default.svc
     clusterResourceWhitelist:
       - group: ''
         kind: Namespace
   EOF
   ```

3. **Repository access** - If using a private repo, ensure ArgoCD has credentials configured

## Apply the Application

```bash
kubectl apply -f argocd/application.yaml
```

## Verify Deployment

### Check application status
```bash
# Using kubectl
kubectl get application ch-users-service -n argocd

# Using ArgoCD CLI
argocd app get ch-users-service
```

### Watch sync status
```bash
argocd app wait ch-users-service --sync
```

### Check deployed resources
```bash
kubectl get all -n contractors-haven -l app.kubernetes.io/name=ch-users-service
```

## Common Operations

### Manual sync
```bash
argocd app sync ch-users-service
```

### Force refresh from Git
```bash
argocd app get ch-users-service --refresh
```

### View application logs
```bash
kubectl logs -n contractors-haven -l app.kubernetes.io/name=ch-users-service -f
```

### Rollback to previous version
```bash
# List history
argocd app history ch-users-service

# Rollback to specific revision
argocd app rollback ch-users-service <revision>
```

### Delete the application
```bash
# This will also delete all managed resources due to cascade delete
kubectl delete application ch-users-service -n argocd

# Or keep resources (just unmanage them)
argocd app delete ch-users-service --cascade=false
```

## Sync Options Explained

| Option | Description |
|--------|-------------|
| `prune: true` | Automatically delete resources removed from Git |
| `selfHeal: true` | Auto-correct cluster drift from desired state |
| `PrunePropagationPolicy=foreground` | Delete dependents before parents |
| `PruneLast=true` | Prune after all other sync operations |
| `ApplyOutOfSyncOnly=true` | Only apply resources that are out of sync |

## Troubleshooting

### Application stuck in "Progressing"
```bash
# Check events
kubectl describe application ch-users-service -n argocd

# Check pod status
kubectl get pods -n contractors-haven
kubectl describe pod -n contractors-haven -l app.kubernetes.io/name=ch-users-service
```

### Sync failed
```bash
# View sync result
argocd app get ch-users-service

# View detailed sync status
argocd app sync ch-users-service --dry-run
```

### Resource out of sync
```bash
# See diff
argocd app diff ch-users-service
```