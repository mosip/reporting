FROM debezium/connect:1.2

LABEL maintainer="MOSIP"

ENV TEST=TEST

ENV	GROUP_ID=mosip-reporting \
	CONFIG_STORAGE_TOPIC=mosip-config-reporting \
	OFFSET_STORAGE_TOPIC=mosip-offset-reporting \
	BOOTSTRAP_SERVERS=kafka-0.kafka-headless.default.svc.cluster.local:9092,kafka-1.kafka-headless.default.svc.cluster.local:9092,kafka-2.kafka-headless.default.svc.cluster.local:9092

ENV CONNECT_NAME=audit-transaction \
	DATABASE_HOSTNAME=dev.mosip.net \
	DATABASE_PORT=30090 \
	DATABASE_USER=audituser \
	DATABASE_PASSWORD= \
	DATABASE_NAME=mosip_audit \
	REPORTING_SERVER=REPORTING-SERVER \
	TABLE_NAME=audit.app_audit_log 

RUN mkdir /kafka/mosip

COPY --chown=kafka properties/connector.properties /kafka/connect/debezium-connector-postgres/ 
COPY --chown=kafka start.sh /kafka/mosip/start.sh 
COPY --chown=kafka connect-log4j.properties /kafka/config/

RUN chmod +x /kafka/mosip/start.sh

#topic is combination of <database.server.name>.<table_name>

CMD /kafka/mosip/start.sh 

