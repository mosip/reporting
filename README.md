# Reporting framework

## Introduction
Reporting framework for real-time streaming data and visualization.  

![](docs/images/reporting_architecture.png)

## Installation in Kubernetes cluster

### Prerequisites

* MOSIP cluster installed as given [here](https://github.com/mosip/mosip-infra/tree/1.2.0_v3/deployment/v3)
* Elasticsearch and Kibana already running in the cluster.
* Postgres installed with `extended.conf`. (MOSIP default install has this configured)

###  Install data pipeline
* Inspect `scripts/values.yaml` for modules to be installed.
* Inspect `scripts/values-init.yaml` for connector configs.
* Run
```sh
cd scripts
./install.sh <kube-config-file>
```
All components will be installed in `reporting` namespace of the cluster.

- NOTE: for the db_user use superuser/`postgres` for now, because any other user would require the db_ownership permission, create permission & replication permission. (TODO: solve the problems when using a different user.)
- NOTE: before installing, `reporting-init` debezium configuration, make sure to include all tables under that db beforehand. If one wants to add another table from the same db, it might be harder later on. (TODO: develop some script that adds additional tables under the same db)

### Upload Kibana dashboards
Various Kibana dashboards are available in `dashboards` folder.  Upload all of them with the following script:
```sh
cd scripts
./load_kibana_dashboards.sh
```
The dashboards may also be uploaded manually using Kibana UI.

## Custom connectors
Install your own connectors as given [here](docs/connectors.md)

## Cleanup/uninstall
To clean up the entire data pipeline follow the steps given [here](docs/cleanup.md)

CAUTION: Know what you are doing!  

## Notes - Kafka Connectors & Transforms

- Debezium, kafka "SOURCE" connector, puts all the (WAL logs) data into kafka in a raw manner, without modifying anything. So there are no "transformations" on source connector side.
- So its the job of whoever it is, that's reading these kafka topics, to modify the data in the way it is desired be put into elasticsearch.
- Currently, we are using Confluent's Elasticsearch Kafka Connector, "SINK" connector, (like debezium, this is also a kafka connector) to put data from kafka topics into elasticsearch indices. But this method also puts the data raw into elasticsearch. (Right now we have multiple sink "connections" between kafka and elasticsearch, almost one for each topic)
- To modify the data, we can use kafka connect's SMTs(Single Message Transforms). Basically each of these transforms change each kafka record in a particular way. So each individual connection can be configured such that these SMTs are applied in a chain.
- Please note that from here on the terms "connetor" and "connection" are used invariably, which mean each kafka-connection (source/sink). All the reference connector configuration files can be found [here](kafka-connect/).
- For how each of these ES connections configures and uses the transforms, and how they are chained, refer to any of the file [here](kafka-connect/ref_connector_api_calls). For more info on the connectors themselves, refer [here](docs/connectors.md).
- Side note: We have explored Spark (i.e., a method that doesn't use kafka sink connectors) to stream these kafka topics and put that data into elasticsearch manually. There are many complications this way. So currently continuing with the ES kafka connect + SMT way
- So the custom transforms that are written, just need to be available in the docker image of the es-kafka-connector. Find the code for these transforms and more details on how to develop and build these transforms and build the es-kafka-connector docker image etc, [here](build/es-kafka-connetor).
