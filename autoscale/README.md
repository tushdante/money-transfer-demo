# Autoscale Demo

Demonstrates Temporal worker autoscaling using KEDA (Kubernetes Event Driven Autoscaler) in a Kubernetes environment.

## Prerequisites

- `kubectl` installed
- `minikube` installed
- Docker installed
- Temporal CLI installed

## Usage

```bash
./demo.sh [--cloud] [--follow] [--encrypt] [--step]
```

### Options

- `--cloud` - Connect to Temporal Cloud (requires `setcloudenv.sh` configuration)
- `--follow` - Watch deployment scaling and KEDA logs in real-time
- `--encrypt` - Enable payload encryption
- `--step` - Pause after each step and wait for user input to continue

### Examples

```bash
# Local Temporal server
./demo.sh

# Temporal Cloud with log following
./demo.sh --cloud --follow

# Local with encryption
./demo.sh --encrypt
```

## What it does

1. Starts minikube cluster
2. Installs KEDA for autoscaling
3. Builds worker Docker image
4. Sets up Kubernetes secrets for Temporal connection
5. Deploys worker with KEDA ScaledObject
6. Starts 20 workflows to trigger scaling

## Monitoring

- **Workflows**: Local at http://localhost:8233 or Temporal Cloud UI
- **Pods**: Use `k9s -n money-transfer`
