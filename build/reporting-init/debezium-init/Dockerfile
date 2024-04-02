FROM alpine

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

RUN apk add curl bash vim

ADD docker-entrypoint.sh /

# ARGs
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# Create user and group
RUN addgroup -g ${container_user_gid} ${container_user_group} || true && \
    adduser -D -u ${container_user_uid} -G ${container_user_group} ${container_user} || true

# Set working directory and permissions
WORKDIR /home/${container_user}
RUN chown -R ${container_user}:${container_user_group} /home/${container_user}

USER ${container_user}:${container_user_group}

CMD ["/docker-entrypoint.sh"]
