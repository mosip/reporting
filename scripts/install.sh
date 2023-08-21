#!/bin/sh
## This script install all required components for reporting framework.
## Usage: ./install.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

## Variables
NS=reporting
CHART_VERSION=12.0.2

# Add helm repos
echo "Adding helm repos"
helm repo add mosip https://mosip.github.io/mosip-helm
helm repo update

# Creating namespace with istio-injeciton
echo "Creating namespace"
kubectl create ns $NS
kubectl label ns $NS istio-injection=enabled

## Configure Postgres essentials
# Assumed Postgres is installed in 'postgres' namespace with extended.conf as extended config.

echo "Copying secrets"
./copy_secret.sh $NS

echo "Installing reporting helm"
helm -n $NS install reporting mosip/reporting -f values.yaml --wait --version $CHART_VERSION

echo "Waiting for helm chart to install"
sleep 30s

echo "Installing reporting-init helm"
INSTALL_NAME=$(kubectl get cm global -o jsonpath={.data.installation-name})
read -p "Give the installation name (Use \"_\" instead of \"-\". And no capitals/symbols.): (default: $INSTALL_NAME) " TO_REPLACE
INSTALL_NAME=${TO_REPLACE:-$INSTALL_NAME}
unset TO_REPLACE

DEBEZ_CONN_FILE="../kafka-connect/debez-sample-conn.api"
read -p "Give the path to debez sample connector file: (default: $DEBEZ_CONN_FILE) " TO_REPLACE
DEBEZ_CONN_FILE=${TO_REPLACE:-$DEBEZ_CONN_FILE}
unset TO_REPLACE

ES_CONN_FOLDER="../kafka-connect/ref_connector_api_calls"
read -p "Give the path to folder containing es connectors: (default: $ES_CONN_FOLDER) " TO_REPLACE
ES_CONN_FOLDER=${TO_REPLACE:-$ES_CONN_FOLDER}
unset TO_REPLACE

kubectl delete cm --ignore-not-found=true debz-conn-confmap -n $NS; kubectl create cm debz-conn-confmap --from-file=$DEBEZ_CONN_FILE -n $NS
kubectl delete cm --ignore-not-found=true es-conn-confmap -n $NS; kubectl create cm es-conn-confmap --from-file=$ES_CONN_FOLDER -n $NS
helm -n $NS install reporting-init mosip/reporting-init --wait --version $CHART_VERSION -f values-init.yaml --set base.db_prefix=$INSTALL_NAME --set debezium_connectors.existingConfigMap=debz-conn-confmap --set es_kafka_connectors.existingConfigMap=es-conn-confmap
