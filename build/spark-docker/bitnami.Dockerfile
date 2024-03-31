FROM bitnami/spark:3

USER root

RUN mkdir /my_spark_jobs
RUN mkdir /my_spark_logs
RUN mkdir /my_spark_full_logs
RUN mkdir /.ivy2
RUN mkdir /.m2
RUN mkdir /opt/bitnami/spark/.ivy2
RUN chown 1001:1001 /my_spark_jobs
RUN chown 1001:1001 /my_spark_logs
RUN chown 1001:1001 /my_spark_full_logs
RUN chown 1001:1001 /.ivy2
RUN chown 1001:1001 /.m2
RUN chown 1001:1001 /opt/bitnami/spark/.ivy2

RUN pip3 install elasticsearch
RUN pip3 install pandas
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001
WORKDIR /home/${container_user}
RUN chown -R ${container_user}:${container_user} /home/${container_user}
USER ${container_user_uid}:${container_user_gid}

USER 1001
