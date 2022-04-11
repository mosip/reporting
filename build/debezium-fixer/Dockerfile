FROM alpine

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

RUN apk add curl bash vim jq \
&& groupadd -g ${container_user_gid} ${container_user_group} \
&& useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/sh -m ${container_user} \
&& mkdir -p /home/${container_user} \
&& chown -R ${container_user}:${container_user} /home/${container_user}

WORKDIR /home/${container_user}

ADD docker-entrypoint.sh .
RUN chown ${container_user}:${container_user} /home/${container_user}/docker-entrypoint.sh

CMD ["./docker-entrypoint.sh"]
