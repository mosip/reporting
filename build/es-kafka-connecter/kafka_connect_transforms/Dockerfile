FROM confluentinc/cp-kafka-connect:7.0.0-1-ubi8

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# Add additional layers
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# Install Kafka Connect Elasticsearch Connector
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:11.1.3

# Copy required JAR files
ADD ./target/kafka_connect_transforms-*.jar /usr/share/java/my_transforms
ADD ./target/debezium-core-*.jar /usr/share/java/my_transforms

# Create user and group
RUN groupadd -g ${container_user_gid} ${container_user_group} && \
    useradd -u ${container_user_uid} -g ${container_user_group} ${container_user}

WORKDIR /home/${container_user}

# Change ownership of the home directory
RUN chown -R ${container_user}:${container_user} /home/${container_user}

# Switch user
USER ${container_user_uid}:${container_user_gid}
