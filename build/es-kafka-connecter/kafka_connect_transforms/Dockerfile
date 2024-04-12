FROM confluentinc/cp-kafka-connect:7.0.0-1-ubi8

# can be passed during Docker build as build time environment variable for mosip user level change.
#ARG container_user=mosip
#ARG container_user_group=mosip
#ARG container_user_uid=1001
#ARG container_user_gid=1001

# can be passed during Docker build as build time environment for label related addition to docker.
#ARG SOURCE
#ARG COMMIT_HASH
#ARG COMMIT_ID
#ARG BUILD_TIME

# can be passed during Docker build as build time environment for label.
#LABEL source=${SOURCE}
#LABEL commit_hash=${COMMIT_HASH}
#LABEL commit_id=${COMMIT_ID}
#LABEL build_time=${BUILD_TIME}

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:11.1.3 
#RUN chown -R ${container_user}:${container_user} /home/${container_user}

ADD ./target/kafka_connect_transforms-*.jar /usr/share/java/my_transforms
ADD ./target/debezium-core-*.jar /usr/share/java/my_transforms

#WORKDIR /home/${container_user}
#USER ${container_user_uid}:${container_user_gid}
