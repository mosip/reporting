FROM alpine:3.15.4

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

RUN apk add curl bash vim jq \
&& addgroup -g ${container_user_gid} ${container_user_group} \
&& adduser -u ${container_user_uid} -G ${container_user_group} -s /bin/sh -D ${container_user}

USER ${container_user}
WORKDIR /home/${container_user}
ADD --chown=${container_user}:${container_user_group} docker-entrypoint.sh .

CMD ["./docker-entrypoint.sh"]
