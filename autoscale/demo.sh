#!/bin/sh

# Constants
KEDA_VERSION="2.17.2"
LOCAL_TEMPORAL_ADDRESS="host.docker.internal:7233"

pause_if_step() {
  if [ "$STEP_MODE" = true ]; then
    echo "Press any key to continue...\n"
    read -n 1 -s
  fi
}

start_minikube() {
  echo "🚀 Starting minikube..."
  minikube status > /dev/null 2>&1 || minikube start
  pause_if_step
}

install_keda() {
  echo "📦 Installing KEDA as a Kubernetes Deployment..."
  kubectl create namespace keda --dry-run=client -o yaml | kubectl apply -f -
  KEDA_EXISTS=$(kubectl get deployment keda-operator -n keda 2>/dev/null)
  kubectl apply --server-side --force-conflicts -f https://github.com/kedacore/keda/releases/download/v${KEDA_VERSION}/keda-${KEDA_VERSION}.yaml > /dev/null
  
  echo "⏳ Waiting for KEDA to be ready. This may take a few minutes..."
  while ! kubectl get pods -l app=keda-operator -n keda 2>/dev/null | grep -q keda-operator; do sleep 5; done
  if [ -z "$KEDA_EXISTS" ]; then
    kubectl get events -n keda --watch &
    EVENT_PID=$!
  fi
  kubectl wait --for=condition=ready pod -l app=keda-operator -n keda --timeout=300s || exit 1
  kubectl wait --for=condition=ready pod -l app=keda-metrics-apiserver -n keda --timeout=300s || exit 1
  if [ -n "$EVENT_PID" ]; then
    kill $EVENT_PID 2>/dev/null
  fi
  pause_if_step
}

build_image() {
  echo "🐳 Building Worker image..."
  eval $(minikube docker-env)
  if docker image inspect money-transfer-worker > /dev/null 2>&1; then
    docker build --quiet -t money-transfer-worker -f Dockerfile .. 2>&1 | grep -v "What's next:"
  else
    docker build -t money-transfer-worker -f Dockerfile .. 2>&1 | grep -v "What's next:"
  fi
  pause_if_step
}

setup_secrets() {
  echo "🔐 Setting up Kubernetes Secrets..."
  kubectl create namespace money-transfer --dry-run=client -o yaml | kubectl apply -f -
  
  if [ "$MODE" = "cloud" ]; then
    # Load Cloud env details for creating secrets
    if [ -f "setcloudenv.sh" ]; then
      . ./setcloudenv.sh
    elif [ -f "../setcloudenv.sh" ]; then
      . ../setcloudenv.sh
    else
      echo "Error: setcloudenv.sh not found in current or parent directory. Please create it from setcloudenv.example."
      exit 1
    fi

    if [ -z "${TEMPORAL_ADDRESS}" ]; then
      echo "Error: TEMPORAL_ADDRESS not set. Please configure setcloudenv.sh with your Temporal Cloud connection details."
      exit 1
    fi
    
    if [ -n "${TEMPORAL_CERT_PATH}" ] && [ -f "${TEMPORAL_CERT_PATH}" ] && [ -n "${TEMPORAL_KEY_PATH}" ] && [ -f "${TEMPORAL_KEY_PATH}" ]; then
      # Create TLS authn secret for worker
      kubectl delete secret temporal-tls -n money-transfer --ignore-not-found
      kubectl create secret tls temporal-tls -n money-transfer \
        --cert="${TEMPORAL_CERT_PATH}" \
        --key="${TEMPORAL_KEY_PATH}"
    fi
    
    # Create other secrets for worker
    kubectl delete secret temporal-secrets -n money-transfer --ignore-not-found
    kubectl create secret generic temporal-secrets -n money-transfer \
      --from-literal=TEMPORAL_ADDRESS="${TEMPORAL_ADDRESS}" \
      --from-literal=TEMPORAL_NAMESPACE="${TEMPORAL_NAMESPACE}" \
      --from-literal=TEMPORAL_API_KEY="${TEMPORAL_API_KEY}" \
      --from-literal=TEMPORAL_CERT_PATH="/etc/ssl/temporal/tls.crt" \
      --from-literal=TEMPORAL_KEY_PATH="/etc/ssl/temporal/tls.key" \
      --from-literal=ENCRYPT_PAYLOADS="${ENCRYPT_PAYLOADS}"
  else
    # Create secrets for connecting to local Temporal server
    kubectl delete secret temporal-secrets -n money-transfer --ignore-not-found
    kubectl create secret generic temporal-secrets -n money-transfer \
      --from-literal=TEMPORAL_ADDRESS="${LOCAL_TEMPORAL_ADDRESS}" \
      --from-literal=TEMPORAL_NAMESPACE="default" \
      --from-literal=ENCRYPT_PAYLOADS="${ENCRYPT_PAYLOADS}"
  fi
  pause_if_step
}

create_scaledobject() {
  echo "📏 Creating ScaledObject for KEDA..."
  
  if [ "$MODE" = "cloud" ]; then
    ENDPOINT="${TEMPORAL_ADDRESS}"
    NAMESPACE="${TEMPORAL_NAMESPACE}"
    
    if [ -n "${TEMPORAL_CERT_PATH}" ] && [ -f "${TEMPORAL_CERT_PATH}" ] && [ -n "${TEMPORAL_KEY_PATH}" ] && [ -f "${TEMPORAL_KEY_PATH}" ]; then
      kubectl apply -f trigger-auth-tls.yaml
      AUTH_REF="temporal-tls-auth"
    elif [ -n "${TEMPORAL_API_KEY}" ]; then
      kubectl apply -f trigger-auth-apikey.yaml
      AUTH_REF="temporal-apikey-auth"
    else
      AUTH_REF=""
    fi
  else
    ENDPOINT="${LOCAL_TEMPORAL_ADDRESS}"
    NAMESPACE="default"
    AUTH_REF=""
  fi
  
  # Create temporary file with substituted values
  TEMP_YAML=$(mktemp)
  sed "s/TEMPORAL_NAMESPACE_PLACEHOLDER/$NAMESPACE/g; s|TEMPORAL_ENDPOINT_PLACEHOLDER|$ENDPOINT|g; s/AUTHENTICATION_REF_PLACEHOLDER/$AUTH_REF/g" scaledobject.yaml > "$TEMP_YAML"
  
  # Remove authenticationRef section if AUTH_REF is empty
  if [ -z "$AUTH_REF" ]; then
    sed -i '' '/authenticationRef:/,+1d' "$TEMP_YAML"
  else
    sed -i '' "s/AUTHENTICATION_REF_PLACEHOLDER/$AUTH_REF/g" "$TEMP_YAML"
  fi

  echo "Final ScaledObject YAML:\n"
  cat "$TEMP_YAML"
  echo ""
  
  kubectl apply -f "$TEMP_YAML"
  rm "$TEMP_YAML"
  pause_if_step
}

deploy_app() {
  echo "⚙️  Deploying the Worker to Kubernetes..."
  kubectl apply -f deployment.yaml
  
  echo "⏳ Waiting for deployment to be ready..."
  if ! kubectl wait --for=condition=available deployment/money-transfer-worker -n money-transfer --timeout=300s; then
    echo "❌ Deployment failed. Checking pod status..."
    kubectl get pods -n money-transfer
    kubectl describe pods -l app=money-transfer-worker -n money-transfer
    exit 1
  fi
  echo "✅ Deployment complete!"
  pause_if_step
}

follow_logs() {
  echo "📊 Watching deployment scaling from 0 to N and back to 0..."
  kubectl get deployment money-transfer-worker -n money-transfer -w &
  DEPLOYMENT_WATCH_PID=$!
  kubectl logs -f -l app=keda-operator -n keda --all-containers=true | grep scaleexecutor &
  KEDA_LOGS_PID=$!
  
  trap 'kill $DEPLOYMENT_WATCH_PID $KEDA_LOGS_PID 2>/dev/null; exit' INT TERM
  wait
}

start_workflows() {
  WORKFLOW_COUNT=20
  echo "🚀 Starting $WORKFLOW_COUNT workflows..."  
  show_info
  for i in $(seq 5 -1 1); do
    echo "Starting $WORKFLOW_COUNT workflows in $i seconds..."
    sleep 1
  done
  
  if [ "$MODE" = "cloud" ]; then
    ENV_FLAG="--env ${TEMPORAL_ENV}"
  else
    ENV_FLAG=""
  fi
  
  for i in $(seq 1 $WORKFLOW_COUNT); do
    temporal workflow start \
      $ENV_FLAG \
      --type AccountTransferWorkflow \
      --task-queue MoneyTransfer \
      --workflow-id TRANSFER-ABC-123-$i \
      --input '{"amount": 45, "fromAccount": "account1", "toAccount": "account2"}' > /dev/null 2>&1 || echo "Error starting workflow $i" >&2
  done
  echo "✅ Started $WORKFLOW_COUNT workflows"
}

show_info() {
  echo "┌────────────────────────────────────────────────────────────────────────────────────────────┐"
  if [ "$MODE" = "cloud" ]; then
    echo "│ 🌐 View workflows:	https://cloud.temporal.io/namespaces/${TEMPORAL_NAMESPACE}/workflows	     │"
  else
    echo "│ 🌐 View workflows:	http://localhost:8233/namespaces/default/workflows	             │"
  fi
  echo "│ 👀 Watch pods:        k9s -n money-transfer				                     │"
  echo "└────────────────────────────────────────────────────────────────────────────────────────────┘"
  pause_if_step
}

# Parse arguments
MODE="local"
FOLLOW_LOGS=false
ENCRYPT_PAYLOADS="false"
STEP_MODE=false

for arg in "$@"; do
  case $arg in
    --cloud)
      MODE="cloud"
      ;;
    --follow)
      FOLLOW_LOGS=true
      ;;
    --encrypt)
      ENCRYPT_PAYLOADS="true"
      ;;
    --step)
      STEP_MODE=true
      ;;
  esac
done

check_kubectl() {
  if ! command -v kubectl > /dev/null 2>&1; then
    echo "❌ kubectl is not installed. Please install kubectl first."
    exit 1
  fi
}

check_directory() {
  if [ "$(basename "$(pwd)")" != "autoscale" ]; then
    echo "❌ This script must be run from the 'autoscale' directory."
    echo "Current directory: $(pwd)"
    echo "Please cd to the 'autoscale' directory and run the script again."
    exit 1
  fi
}

# Main execution
check_directory
check_kubectl
start_minikube
install_keda
build_image
setup_secrets
deploy_app
create_scaledobject
start_workflows
if [ "$FOLLOW_LOGS" = true ]; then
  follow_logs
fi
