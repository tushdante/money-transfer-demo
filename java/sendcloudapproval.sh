#!/bin/bash
# This is using my temporal email
export TEMPORAL_ADDRESS=rick-ross.a2dd6.tmprl.cloud:7233
export TEMPORAL_NAMESPACE=rick-ross.a2dd6
# this is with my personal email account
# export TEMPORAL_ADDRESS=rick-ross-dev.sdvdw.tmprl.cloud:7233
# export TEMPORAL_NAMESPACE=rick-ross-dev.sdvdw
export TEMPORAL_CERT_PATH="/Users/rickross/dev/samples-server/tls/client-only/mac/rick-ross.pem"
export TEMPORAL_KEY_PATH="/Users/rickross/dev/samples-server/tls/client-only/mac/rick-ross.key"
ENCRYPT_PAYLOADS=$2 ./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.TransferApprover -Parg=$1
# where TRANSFER-EZF-249 is the workflowId
#./gradlew -q execute -PmainClass=io.temporal.samples.moneytransfer.TransferApprover -Parg=$1
