FROM alpine

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


RUN apk add curl bash vim \
&& mkdir /connectors /kibana_saved_objects

ADD docker-entrypoint.sh /

WORKDIR /home/${container_user}

USER ${container_user_uid}:${container_user_gid}

RUN chown -R ${container_user}:${container_user} /home/${container_user} 

CMD ["/docker-entrypoint.sh"]
