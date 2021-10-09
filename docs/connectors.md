# Connectors

## Custom connectors
To install custom  connectors follow the methods given below. 

The methods here will NOT add automatically DB tables to debezium. Add your tables (if not already present) by editing a connnector's config.

### Method 1:
* Put the new elasticsearch connectors in one folder.
    * Create a configmap with for this folder, using:
    ```sh
    kubectl create configmap <conf-map-name> -n reporting --from-file=<folder-path>
    ```
    * Edit `values-init.yaml` to use the above configmap:
    ```
    es_kafka_connectors:
        existingConfigMap: <conf-map-name>
    ```
* Edit `values-init.yaml`, the debezium_connectors for new dbs and tables. Or disable if not required.
    * Can also use a custom debezium connector using the following. (Not recommended)
    * Create a configmap with custom debezium connector:
    ```
    $ kubectl create configmap <conf-map-name> -n reporting --from-file=<path-for-debez-connector>
    ```
    - Edit `values-init.yaml` to use the above configmap:
    ```
    debezium_connectors:
        existingConfigMap: <conf-map-name>
    ```
* Install `reporting-init` again:
```
helm -n reporting delete reporting-init
helm -n reporting install reporting-init mosip/reporting-init -f values-init.yaml
```

### Method 2 (manual):

* Edit `./sample_connector.api` file, accordingly. And run the following:
```sh
./run_connect_api.sh sample_connector.api <kube-config-file>
```
