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

# Change ownership of /home/mosip if it hasn't been changed yet
RUN chown -R ${container_user}:${container_user} /home/${container_user}

# Set the working directory and switch to the specified user
WORKDIR /home/${container_user}

# Add user and group if they don't exist
RUN if ! getent group ${container_user} >/dev/null; then \
        addgroup -g ${container_user_gid} ${container_user_group}; \
    fi && \
    if ! getent passwd ${container_user} >/dev/null; then \
        adduser -D -u ${container_user_uid} -G ${container_user_group} ${container_user}; \
    fi

USER ${container_user}
USER ${container_user_uid}:${container_user_gid}

CMD ["/docker-entrypoint.sh"]
