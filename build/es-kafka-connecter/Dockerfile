FROM confluentinc/cp-kafka-connect:7.0.0-1-ubi8

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:11.1.3

ADD ./jars /usr/share/java/my_transforms
