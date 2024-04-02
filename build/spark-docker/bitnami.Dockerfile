FROM bitnami/spark:3

ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001
WORKDIR /home/${container_user}

USER root

RUN mkdir /my_spark_jobs \
&& mkdir /my_spark_logs \
&& mkdir /my_spark_full_logs \
&& mkdir /.ivy2 \
&& mkdir /.m2 \
&& mkdir /opt/bitnami/spark/.ivy2 \
&& chown 1001:1001 /my_spark_jobs \
&& chown 1001:1001 /my_spark_logs \
&& chown 1001:1001 /my_spark_full_logs \
&& chown 1001:1001 /.ivy2 \
&& chown 1001:1001 /.m2 \
&& chown 1001:1001 /opt/bitnami/spark/.ivy2 \
&& chown -R ${container_user}:${container_user} /home/${container_user} \
&& pip3 install elasticsearch \
&& pip3 install pandas

USER 1001

USER ${container_user_uid}:${container_user_gid}
