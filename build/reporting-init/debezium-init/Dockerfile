FROM alpine

# can be passed during Docker build as build time environment variable for mosip user level change.
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# can be passed during Docker build as build time environment for label related addition to docker.
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME

# can be passed during Docker build as build time environment for label.
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

ADD docker-entrypoint.sh /

# Set working directory and permissions
WORKDIR /home/${container_user}

# Create user and group
RUN apk add curl bash vim && \
    addgroup -g ${container_user_gid} ${container_user_group} || true && \
    adduser -D -u ${container_user_uid} -G ${container_user_group} ${container_user} || true && \
    chown -R ${container_user}:${container_user} /home/${container_user}

USER ${container_user_uid}:${container_user_gid}

CMD ["/docker-entrypoint.sh"]
