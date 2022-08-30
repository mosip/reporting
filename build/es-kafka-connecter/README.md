## Docker for Elasticsearch kafka connector

This folder contains instructions and build material for the docker for elasticsearch kafka connector

This docker image contains 3 components
1. Elasticsearch Kafka Connector installed from confluent hub
1. Debezium-Core jar file, for using `ExtractNewField` transform
    - This is downloaded form mvn repo
1. An uber-jar that contains the following transforms and their Requirements:
    - `io.mosip.kafka.connect.transforms.TimestampConverterAdv`, which is built using the existing `org.apache.kafka.connect.transforms.TimstampConverter`, this converts epochs in INT64 to string format the way we want
    - `io.mosip.kafka.connect.transforms.DynamicNewField`, which is built using the existing `org.apache.kafka.connect.transforms.InsertField`, this adds a new field in to the entry by performing a query using the existing fields (currently only elasticsearch can be performed)
    - `io.mosip.kafka.connect.transforms.StringToJson`, this converts json encoded as string to json
    - `io.mosip.kafka.connect.transforms.TimestampSelector`, this selects a field based on the given list of fields in priority, and creates a new field with the selected value. (Used when there are multiple timestamps and on wants to have new generic timestamp, based upon the availability of list of timestamps in order)
    - `io.mosip.kafka.connect.transforms.AnonymousProfileTransform`, transform to convert anonymous profile data

### Dev Notes:

- To build all the transforms into an uber-jar just use `mvn clean package` in the `kafka_connect_transforms` directory.
- How build the docker image using the transforms:
  ```sh
  rm -r jars
  cd kafka_connect_transforms ; mvn clean package ; cd ..
  mkdir jars; cd jars
  cp ../kafka_connect_transforms/target/kafka_connect_transforms*.jar .
  wget https://repo.maven.apache.org/maven2/io/debezium/debezium-core/1.7.0.Final/debezium-core-1.7.0.Final.jar
  cd ..
  docker build .
  ```
- To test the transforms locally write Junit tests, take the sample src code from any one if the available tests.

<details><summary>Here are some pointers on how to get started on developing new transforms:</summary>

  - There are 3 main functions in every transform, `configure`, `apply`, `close`.
  - `configure` is run once while initializing the connector, `apply` is run for every record coming from kafka, `close` is run once in the end while closing the connection. (Also `config` function, different from `configure`, not very important). Rest of the functions are all defined ourself.
  - In every kafka record/message, there is a "key" and a "value". The ES-kafka-connector treats the `key` part of the message as the `_id` for the document going into elasticsearch index, and the `value` part of the message as the document itself. (Note `key` will only be treated as `id` if this configuration option is false. Otherwise its dealt differently. `"key.ignore": "false"` ).
  - So the transformation has to have the ability to be applied to either Key or value seperately. To distinguish this, each transform has subclasses "Key" and "Value". To configure the same on the connector conf, use on of the following:
  ```
  transforms.myTransform.type: com.my.Transform$Value
  transforms.myTransform.type: com.my.Transform$Key
  ```
  - Each key and value, can be treated as different types using "Converters". Here is an example configuration. The following means that we are asking kafka-connect to treat the whole key as basically just a string.
  ```
  "key.converter": "org.apache.kafka.connect.storage.StringConverter"
  ```
  Whereas the following means that we are asking kafka-connect to treat the whole key itself a JSON.
  ```
  "key.converter": "org.apache.kafka.connect.json.JsonConverter"
  ```
  Similarly `value.converter` options can also be used.
  - In Kafka, there is something called `Schema`, lets discuss. Schema is basically metadata that gives structure to the data.
  - In the case of using `org.apache.kafka.connect.json.JsonConverter`, Schema basically means an extra json accompanying the original json telling what is the type of each field/entry in the original json.
  - So when a schemaless json looks like this;
  ```
  {"a": 10, "b": "hello"}
  ```
    A schema-ed json looks this:
  ```
  {"schema":{//meta_info_about_type_of_a_type_of_b//},"payload":{"a": 10, "b": "hello"}}
  ```
  - Now:
    1. This schema has to come as part of the data from source (debezium in our case). This schema can also be injested in some way using a schema registry, but totally not required in our case. Debezium already infers the db schema for all the columns and put thats.
    1. Key and Value can both be independent json having there own schemas.
    1. Kafka Connect treats schema-ed data and Schema-less data differently. Because in schema-ed data, to add a new field to the json its schema has to be first present/added ourself.
  - Why schema? It looks difficult to deal with, sure. One reason for using schema:
  ```
  "schema.ignore": "false"
  ```
  With the above option set to false, ES-kafka-connector will get the mapping for each field from the schema of that field. If the above is set to `"true"`, the schema of the field will be ignored, and elasticsearch will use its dynamic-mapping to determine mapping of fields. So schema will be useful when one wants to inject custom mapping for the field.
  - Suggestion: Although the currently built transforms are equipped to handle schemas also (expect for a few). It is suggested to use `"schema.ignore": "true"`, and then let elasticsearch do dynamic-mapping, because one has to get into the hassle of managing the schema seperately. Which can become quite difficult in some cases. (Like the `AnonymousProfileTransform`)
  - In the transform there are two functions, `applySchemaless`, `applyWithSchema`. Where we will determine whether the data has schema or not, and process it accordingly.
  - In case of using `JSONConverter`, and `"schema.ignore": "true"`:
  ```
  "key.converter.schemas.enable": "false",
  ```
  when above option is set to false, the json will be treated as schema-less json, even if it has schema.
  - So, with this above option set to false, in the transform one can focus on building their logic in `applySchemaless`, and ignore the `applyWithSchema` function.
  - The advantage with this approach is that, in the transform, Schema-ed Json data will come as kafka-connect's own `Struct` vs Schemaless json data will come as a simple java `Map<String, Object>` which gives a lot more flexibility.
  - But debezium will put schema into the kafka messages at the source side. So in the case where we want to couple that with option to treat it as schemaless json, first use the `org.apache.kafka.connect.transforms.ExtractField` transform to extract the `"payload"` json out. And then apply the rest of the transforms. For better understanding look at the difference in configuration between `regprc_anon_profile` connector and any other connector like `prereg_applicant_demographic`.
  - Side notes on elasticsearch `_id`:
    - Dealing with the `_id`/"key" of the message is important because, if a new message comes with the same `id` and with a different value elasticsearch will UPDATE the existing document.
    - A `tombstone` message means a kafka message that has a key but has null/no value. Debezium is configured to send such messages in cases where there is a delete on DB. The following configuration option is used to delete that particular document from the ES index if a message comes with null "value" and a valid "key":
    ```
    "behavior.on.null.values": "DELETE"
    ```
    - for the `_id`, elasticsearch WONT accept jsons or any other complicated structures, it has to be a primitive like integer/string, etc. So at the end of all transformations on "Key" of the message, make sure it returns a primitive object like integer/string.
    - Right now the whole key is left as a string unchanged, because with/without schema, that whole string (which is a json) is unique to this document anyway.

</details>
