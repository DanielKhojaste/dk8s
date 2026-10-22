#!/bin/sh
# Creates the DK8s queues and their topic subscriptions on the Solace broker,
# using the broker's SEMP v2 REST management API. Safe to run more than once.
set -eu

SEMP="http://solace:8080/SEMP/v2/config/msgVpns/default"
AUTH="admin:${SOLACE_ADMIN_PASSWORD:-admin}"

# POST a JSON body; treat "already exists" as success so re-runs are harmless.
semp_post() {
  path="$1"
  body="$2"
  code=$(curl -sS -o /tmp/semp-response -w '%{http_code}' -u "$AUTH" \
    -H 'Content-Type: application/json' -X POST "$SEMP$path" -d "$body")
  if [ "$code" = "200" ]; then
    echo "created: $path $body"
  elif grep -q ALREADY_EXISTS /tmp/semp-response; then
    echo "exists:  $path $body"
  else
    echo "FAILED ($code): $path $body"
    cat /tmp/semp-response
    exit 1
  fi
}

create_queue() {
  semp_post "/queues" "{\"queueName\":\"$1\",\"accessType\":\"exclusive\",\"permission\":\"consume\",\"ingressEnabled\":true,\"egressEnabled\":true,\"maxRedeliveryCount\":3}"
}

subscribe() {
  semp_post "/queues/$1/subscriptions" "{\"subscriptionTopic\":\"$2\"}"
}

# The worker consumes deployment requests for every service.
create_queue "dk8s.worker.deployment-requests"
subscribe    "dk8s.worker.deployment-requests" "dk8s/deployment/requested/>"

# control-api consumes the worker's progress and results.
create_queue "dk8s.control-api.deployment-updates"
subscribe    "dk8s.control-api.deployment-updates" "dk8s/deployment/started/>"
subscribe    "dk8s.control-api.deployment-updates" "dk8s/deployment/completed/>"
subscribe    "dk8s.control-api.deployment-updates" "dk8s/deployment/failed/>"

echo "Solace provisioning complete."
