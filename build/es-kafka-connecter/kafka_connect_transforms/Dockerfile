FROM confluentinc/cp-kafka-connect:7.0.0-1-ubi8

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

WORKDIR /home/${container_user}

RUN chown -R ${container_user}:${container_user} /home/${container_user} \
&& confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:11.1.3

ADD ./target/kafka_connect_transforms-*.jar /usr/share/java/my_transforms
ADD ./target/debezium-core-*.jar /usr/share/java/my_transforms

USER ${container_user_uid}:${container_user_gid}
