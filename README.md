# Reporting framework

## Introduction
Reference reporting framework for real-time streaming data and visualization.  

![](docs/images/reporting_architecture.png)

## Installation in Kubernetes cluster

### Prerequisites
 
* MOSIP cluster installed as given [here](https://github.com/mosip/mosip-infra/tree/1.2.0_v3/deployment/v3)
* Elasticsearch and Kibana already running in the cluster. 
* Postgres installed with `extended.conf` as extended config. (MOSIP default install has this configured)

###  Install
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

## Upload Kibana dashboards
Various Kibana dashboards are available in `dashboards` folder.  Upload all of them with the following script:
```sh
cd scripts
./load_kibana_dashboards.sh
```
The dashboards may also be uploaded manually using Kibana UI.

## Custom connectors
Install your own connectors as given [here](docs/connectors.md)

## Cleanup/uninstall
To clean up the entire data pipeline follow the steps give [here](docs/cleanup.md)

CAUTION: Know what you are doing!  

