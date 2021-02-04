#!/bin/sh
contents=\"\"\"`cat /kafka/connect/debezium-connector-postgres/connector.properties`\"\"\"; python  -c "import os;print $contents.format(**os.environ)" > /kafka/connect/debezium-connector-postgres/connector.properties
/kafka/bin/connect-distributed.sh /kafka/connect/debezium-connector-postgres/connector.properties
