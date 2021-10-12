# Cleanup

## Delete reporting components
```sh
helm delete reporting-init -n reporting
helm delete reporting -n reporting
kubectl delete ns reporting
```
## Postgres cleanup
* List replication replication slots.
```
postgres=# select * from pg_replication_slots;
```
* Delete each of the above slots.
```
postgres=# select pg_drop_replication_slot('<slot_name>');
```
* Go to each database and drop publication.
```
postgres=# \c <db>
postgres=# select * from pg_publication;
postgres=# drop publication <pub_name>;
```
## Kafka Cleanup
* It is recommended to cleanup all topics related to reporting in Kafka as the data will anyway be there in DB/Elasticsearch
* Delete all the relavent topics and Debezium and ES Kafka connectors' `status`, `offset` and `config` topics.

##  Elasticsearch and Kibana Cleanup
* Delete the Elasticsearch indices and dashboards from Kibana UI.
