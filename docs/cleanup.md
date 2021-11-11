# Cleanup

## Delete reporting components
```sh
helm delete reporting-init -n reporting
helm delete reporting -n reporting
kubectl delete ns reporting
```
## Postgres cleanup
* Go to each database and drop publication.
```
postgres=# \c <db>
postgres=# select * from pg_publication;
postgres=# drop publication <pub_name>;
```

## Kafka Cleanup
* It is recommended to cleanup all topics related to reporting in Kafka as the data will anyway be there in DB/Elasticsearch
* Delete/reset all the relavent topics and Debezium and ES Kafka connectors' `status`, `offset` and `config` topics.

##  Elasticsearch and Kibana Cleanup
* Delete the Elasticsearch indices and Kibana dashboards from Kibana UI.
