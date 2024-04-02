FROM alpine

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

RUN apk add --no-cache curl bash vim

RUN mkdir -p /connectors /kibana_saved_objects

ADD docker-entrypoint.sh /

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# Create the user and group
RUN addgroup -g ${container_user_gid} ${container_user_group} && \
    adduser -D -u ${container_user_uid} -G ${container_user_group} ${container_user}

# Change ownership of /home/mosip if it hasn't been changed yet
RUN chown -R ${container_user}:${container_user} /home/${container_user}

# Set the working directory and switch to the specified user
WORKDIR /home/${container_user}

USER ${container_user}

CMD ["/docker-entrypoint.sh"]
