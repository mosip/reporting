# Connectors

## Structure

- `reporting` helm chart is only supposed to install debezium, es-kafka-connector deployments, and other components.
- `reporting-init` helm chart is the actual one that is going to initailize/setup the debezium connections and es connections.  
- There is one debezium connector for each postgres db. Multiple tables of the db are whitelisted under the same connector.
- There is one es-kafka connector for each table (or topic, in kafka terms), unlike above, because the transforms applied maybe be different from table to table, even if they are under the same db (maybe the general configuration itself is also different).
- The debezium(source) connector configuration is literally same for all the dbs, except for their names. So only one file can be used for all the debezium connectors. Find that [here](../kafka-connect/debez-sample-conn.api). `Reporting-debezium-init` just applies the same file in a loop for all the dbs in the list given in `values-init.yaml` in `debezium_connectors.dbs`.
- Unlike the above, each ES-kafka connection has to be handled/configured differently. Hence multiple connector files. Find those reference files [here](../kafka-connect/ref_connector_api_calls/). `Reporting-ES-init` goes through, applies each of them. And these files are mounted inside the `es-init-pod` by creating a configmap for these files and mounting that on the pod. Refer the following section on how to do that.

## Custom connectors
To install custom  connectors follow the methods given below.

The methods here will NOT automatically add/remove extra DB tables to existing Debezium connectors. Add your extra tables (if not already present) by editing a connnector's config.

Method#1 is what is used in the actual [install.sh](../scripts/install.sh) also.

### Method 1:
* Put the edited/new elasticsearch connectors in one folder.
    * Create a configmap with for this folder, using:
    ```sh
    kubectl create configmap <es-conns-conf-map-name> -n reporting --from-file=<es-conns-folder-path>
    ```
    * Edit `values-init.yaml` to use the above configmap:
    ```
    es_kafka_connectors:
        existingConfigMap: <es-conns-conf-map-name>
    ```
* Edit `values-init.yaml`, the debezium_connectors for new dbs and tables. Or disable if not required.
    * Can also use a custom debezium connector using the following. (Not recommended). If not required ignore this and proceed to next step.
    * Create a configmap with custom debezium connector:
    ```sh
    kubectl create configmap <custom-debez-conn-conf-map-name> -n reporting --from-file=<path-for-debez-connector>
    ```
    - Edit `values-init.yaml` to use the above configmap:
    ```
    debezium_connectors:
        existingConfigMap: <custom-debez-conn-conf-map-name>
    ```
* Make sure to configure the `base` section in `values-init.yaml` properly (if required) before installing.
* Install `reporting-init` again:
```
helm -n reporting delete reporting-init
helm -n reporting install reporting-init mosip/reporting-init -f values-init.yaml
```

### Method 2 (manual):

* This is a legacy method, but will work nevertheless.
* Edit `./es-sample-conn.api` file, accordingly (Especially the commented out section in the beginning). And run the following:
```sh
./run_connect_api.sh es-sample-conn.api <kube-config-file>
```
