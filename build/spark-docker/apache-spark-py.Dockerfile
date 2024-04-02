#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#ARG container_user=mosip
#ARG container_user_group=mosip
#ARG container_user_uid=1001
#ARG container_user_gid=1001
#WORKDIR /home/${container_user}
#RUN chown -R ${container_user}:${container_user} /home/${container_user}
#USER ${container_user_uid}:${container_user_gid}

ARG base_img

FROM $base_img
WORKDIR /

# Reset to root to run installation tasks
USER 0

RUN mkdir ${SPARK_HOME}/python
RUN apt-get update && \
    apt install -y python3 python3-pip && \
    pip3 install --upgrade pip setuptools && \
    # Removed the .cache to save space
    rm -r /root/.cache && rm -rf /var/cache/apt/*

RUN pip3 install elasticsearch && \
    pip3 install pandas && \
    rm -r /root/.cache && rm -rf /var/cache/apt/*

COPY python/pyspark ${SPARK_HOME}/python/pyspark
COPY python/lib ${SPARK_HOME}/python/lib

WORKDIR /opt/spark/work-dir
ENTRYPOINT [ "/opt/entrypoint.sh" ]

# Specify the User that the actual main process will run as
ARG spark_uid=185
ARG spark_jobs_folder=/my_spark_jobs

RUN mkdir ${spark_jobs_folder} && \
    mkdir /opt/spark/.ivy2 && \
    chown ${spark_uid}:${spark_uid} ${spark_jobs_folder} && \
    chown ${spark_uid}:${spark_uid} /opt/spark/.ivy2

USER ${spark_uid}

RUN echo "print(\"JustForPrereq\")" > /tmp/prereq.py; \
    /opt/spark/bin/spark-submit --conf spark.jars.ivy=/opt/spark/.ivy2 --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.0 /tmp/prereq.py; \
    exit 0;
