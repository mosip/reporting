#!/bin/sh

if [ $# -lt 1 ] ; then
  echo "Usage: ./delete_kibana_dashboards.sh <dashboards folder> [kubeconfig file]"
  exit 1
fi

if [ $# -ge 2 ] ; then
  export KUBECONFIG=$2
fi

KIBANA_URL=$(kubectl get cm global -o jsonpath={.data.mosip-kibana-host})
read -p "Give Kibana Host Name (Example: \"kibana.sandbox.mosip.net\" or \"box.mosip.net/kibana\"): (default: $KIBANA_URL) " TO_REPLACE
KIBANA_URL=${TO_REPLACE:-$KIBANA_URL}
unset TO_REPLACE

for file in ${1%/}/*.ndjson ; do
  echo "Loading : $file"
  IFS=$'\n' larray=($(cat $file));
  for line in "${larray[@]}"; do
    type=$(jq -r '.type' <<< $line)
    id=$(jq -r '.id' <<< $line)
    if [ "$type" != "null" ]; then
      echo "Deleting ${type}. id - ${id}"
      curl -XDELETE "https://${KIBANA_URL%/}/api/saved_objects/${type}/${id}"
    fi
  done
done
