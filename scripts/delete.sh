#!/usr/bin/env bash
# Uninstalls reporting
## Usage: ./delete.sh [kubeconfig]

if [ $# -ge 1 ] ; then
  export KUBECONFIG=$1
fi

NS=reporting
read -p "Are you sure you want to delete reporting helm charts?(Y/n) " yn
if [ $yn = "Y" ] ; then
  helm -n $NS delete reporting
  helm -n $NS delete reporting-init
  kubectl delete ns $NS
fi
read -p "Do you want to delete reporting indices from elasticsearch & kibana?(Y/n) " yn
if [ $yn = "Y" ] ; then
  INDEX_PREFIX=$(kubectl get cm global -o jsonpath={.data.installation-name})
  read -p "Give the prefix of the indices to delete: (default: $INDEX_PREFIX) " TO_REPLACE
  INDEX_PREFIX=${TO_REPLACE:-$INDEX_PREFIX}
  kubectl exec -it elasticsearch-master-0 -n cattle-logging-system -- curl -XDELETE "elasticsearch-master:9200/${INDEX_PREFIX}.*"
fi
