FROM confluentinc/cp-kafka-connect:7.0.0-1-ubi8

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

WORKDIR /home/${container_user}

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:11.1.3 \
&& chown -R ${container_user}:${container_user_group} /usr/share/confluent-hub-components \
&& chown -R ${container_user}:${container_user_group} /home/${container_user}

ADD ./target/kafka_connect_transforms-*.jar /usr/share/java/my_transforms
ADD ./target/debezium-core-*.jar /usr/share/java/my_transforms

USER ${container_user_uid}:${container_user_gid}
