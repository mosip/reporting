FROM alpine

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

WORKDIR /home/${container_user}

RUN chown -R ${container_user}:${container_user} /home/${container_user} \
&& apk add curl bash vim \
&& mkdir /connectors /kibana_saved_objects

USER ${container_user_uid}:${container_user_gid}

ADD docker-entrypoint.sh /

CMD ["/docker-entrypoint.sh"]
