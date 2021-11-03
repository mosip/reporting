## Docker for Elasticsearch kafka connector

This folder contains instructions and build material for the docker for elasticsearch kafka connector

This docker image contains 4 key components
1. Elasticsearch Kafka Connector installed from confluent hub
1. Debezium-Core jar file, for using `ExtractNewField` transform
    - This is downloaded form mvn repo
1. An uber-jar that contains all the transforms and their Requirements. Here is a list of transforms
    - `io.mosip.kafka.connect.transforms.TimestampConverterAdv`, which is built using the existing `org.apache.kafka.connect.transforms.TimstampConverter`, this converts epochs in INT64 to string format the way we want
    - `io.mosip.kafka.connect.transforms.DynamicNewField`, which is built using the existing `org.apache.kafka.connect.transforms.InsertField`, this adds a new field in to the entry by performing a query using the existing fields (currently only elasticsearch can be performed)
    - `io.mosip.kafka.connect.transforms.StringToJson`, this converts json encoded as string to json
    - `io.mosip.kafka.connect.transforms.TimestampSelector`, this selects a field based on the given list of fields in priority, and creates a new field with the selected value. (Used when there are multiple timestamps and on wants to have new generic timestamp, based upon the availability of list of timestamps in order)
    - `io.mosip.kafka.connect.transforms.AnonymousProfileTransform`, transform to convert anonymous profile data

Then it copies these jar files to build docker image

```sh
$ rm -r jars
$ cd kafka_connect_transforms ; mvn clean package ; cd ..
$ mkdir jars; cd jars
$ cp ../kafka_connect_transforms/target/the_transforms*.jar .
$ wget https://repo.maven.apache.org/maven2/io/debezium/debezium-core/1.7.0.Final/debezium-core-1.7.0.Final.jar
$ cd ..
$ docker build .
```
