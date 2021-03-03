### 1.	Introduction - Reference Reporting Framework
Document is to present the MOSIP reference reporting framework set-up and deployment. Reporting framework uses below tool for real-time streaming data and visualization.

#### Batch data processing, Persistant Store and Visualisation

##### * Postgres for MOSIP data source enabled with read-replicas for reporting purpose
##### * Logstash is part of elastic stack and used to crawl data from database transform and index to elastic search (Batch processing). Logstash is not required if Debezium, Kafka and spark used (real-time processing).
##### * Elastic search as data-index and persistence store
##### * Kibana as visualization to create and view dashboards and reports. Reference dashboards and reports are provided as part of this deployment.

![MOSIP Reference Reporting Architecture - Batch Data Processing](reporting-framework/reporting-architecture-batch.png)

#### Real time stream data processing, Persistant Store and Visualisation

##### * Postgres for MOSIP data source enabled with binary or write ahead logs
##### * Debezium for change data capture from postgres, This is used along with Kafka connect as plugin
##### * Kafka connect to connect data source and stream data
##### * Kafka server for message broaker which will stream data from kafka connect
##### * Zookeeper for Kafka broakers co-ordination of the jobs
##### * Spark streaming to process the data received from kafka topic in real-time
##### * Spark uses pyspark for data processing and processing job are written in python.
##### * Elastic search as data-index and persistence store
##### * Kibana as visualization to create and view dashboards and reports. Reference dashboards and reports are provided as part of this deployment.

![MOSIP Reference Reporting Architecture - Real Time Data Processing](reporting-framework/reporting-architecture-realtime.png)   

### 2.	Deployment of Elasticsearch and Kibana
#### a.	Java Installation
		$sudo yum install java

#### b.	ElasticSearch Installation and Set-up
#####	1.	Create a file called "`elasticsearch.repo`" in the "`/etc/yum.repos.d/`"
		$cd /etc/yum.repos.d/
		$sudo vi elasticsearch.repo

#####	2. Copy below content on to "`elasticsearch.repo`" file

		[elasticsearch-7.x]
		name=Elasticsearch repository for 7.x packages
		baseurl=https://artifacts.elastic.co/packages/7.x/yum
		gpgcheck=1
		gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
		enabled=1
		autorefresh=1
		type=rpm-md

#####	3. Run command to install elastic search
        $sudo yum install elasticsearch            
        $sudo chkconfig --add elasticsearch

#####	4. Update the file `/etc/elasticsearch/elasticsearch.yml` as below:
		$sudo vi /etc/elasticsearch/elasticsearch.yml
		network.host: xxx.xx.xx.xx <Internal_ip>
		http.port: 9200
		discovery.seed_hosts: ["<Internal_ip>", "*.*.*.*", "host1", "host2"]
		discovery.type: single-node

#####	5. Start elastic search using below command
		$sudo -i service elasticsearch start

#####	6. Log file in "`/var/log/elasticsearch/`"

#####	7. Test elastic search with command curl -X GET "<internal_ip>:9200/?pretty"
		Example: $curl -X GET “<internal_ip>:9200/?pretty”

#####	8. Elastic Search URL http://<public_ip>:9200
		Example: http://xxx.xx.xxx.xx:9200/

#### c.	Kibana Installation and Set-up

#####   1. Create a file called "`kibana.repo`" in the "`/etc/yum.repos.d/`"
		$cd /etc/yum.repos.d/
		$sudo vi kibana.repo

#####   2. Copy below content on to "`kibana.repo`" file

		[kibana-7.x]
		name=Kibana repository for 7.x packages
		baseurl=https://artifacts.elastic.co/packages/7.x/yum
		gpgcheck=1
		gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
		enabled=1
		autorefresh=1
		type=rpm-md
	
#####	3. Run command to install Kibana `sudo yum install kibana`
		$sudo yum install kibana
        $sudo chkconfig --add kibana

#####   4. Update "`kibana.yml`" at /etc/kibana/ with sample values given below
		$sudo vi /etc/kibana/kibana.yml
		server.port: 5601
		server.host: <internal_ip>
		elasticsearch.hosts: ["http://<internal_ip>:9200"]

#####   6. Start kibana using below command
		$sudo -i service kibana start

#####   7. Kibana URL `http://<public_ip>:5601`

#### d.	Logstash Installation and Set-up (Optional: Not required for real-time processing) for Batch Data Processing.

#####   1. Create a file called "`logstash.repo`" in the "`/etc/yum.repos.d/`"
		$cd /etc/yum.repos.d/
		$sudo vi logstash.repo

#####   2. Copy below content on to "`logstash.repo`" file

		[logstash-7.x]
		name=Elastic repository for 7.x packages
		baseurl=https://artifacts.elastic.co/packages/7.x/yum
		gpgcheck=1
		gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
		enabled=1
		autorefresh=1
		type=rpm-md
	
#####	3. Run command to install Kibana "sudo yum install logstash"
		$sudo yum install logstash

#####   4. Copy	"postgresql-42.2.14.jar" in to below location - Note: download jar using curl on to server
			/usr/share/logstash/logstash-core/lib/jars/
	
#### e.	Setting-up Security and Authentication
#####	1. Install x-pack security plugin
		It is installed with elastic by default with latest version of elastic search.   
		Can check with `curl -X GET http://<internal_ip>:9200/_xpack?pretty`

#####	2. Add below config parameters to  "`elaticsearch.yml`" at `/etc/elasticsearch/`.
		$sudo vi /etc/elasticsearch/elasticsearch.yml`
		xpack.security.enabled: true
		xpack.security.transport.ssl.enabled: true
	
#####	3. Restart elasticsearch
		$sudo -i service elasticsearch stop
		$sudo -i service elasticsearch start
	
#####	4. Change Elastic Passwords
		$sudo /usr/share/elasticsearch/bin/elasticsearch-setup-passwords interactive
	
		$curl -u <username>:<password> -X GET "xxx.xx.xx.xx:9200/?pretty"
	
		Example:
			$curl -u elastic:elastic -X GET "xxx.xx.xx.xx:9200/?pretty"
			$curl -u elastic:elastic "xxx.xx.xx.xx:9200/_cat/indices?v"
			$curl -u elastic:elastic -XDELETE xxx.xx.xx.xx:9200/index-name
	
#####	5. Change Kibana configuration at `/etc/kibana/kibana.yml`.
		$sudo vi /etc/kibana/kibana.yml
		elasticsearch.username: "elastic"	
		elasticsearch.password: "elastic"
	
#####	6. Restart kibana
		$sudo -i service kibana stop
		$sudo -i service kibana start

		http://<external_ip>:5601/
	
### 3.	Set-up of Real-time data streaming

#### 1.	Zookeeper Installation and Configuration

#####	1. Download zookeeper latest version (last updated 18/02/21) and extract
		$mkdir ~/zookeeper
		$cd ~/zookeeper
		$wget https://apache.mirrors.nublue.co.uk/zookeeper/zookeeper-3.6.2/apache-zookeeper-3.6.2-bin.tar.gz
       	$tar -zxvf apache-zookeeper-3.6.2-bin.tar.gz
		$cd apache-zookeeper-3.6.2-bin

#####	2. Create zookeper configuration file with below content
		$vi conf/zoo.cfg
		tickTime = 2000
		dataDir = <user home dir full path>/zookeeper/apache-zookeeper-3.6.2-bin/data
		clientPort = 2181
		initLimit = 5
		syncLimit = 2
 
#### 2.	Kafka Server Installation and Configuration 

#####	1. Download latest _binary_ version of kafka (last updated 18/02/21) and extract
		$mkdir ~/kafka
		$cd ~/kafka
		$wget https://apache.mirrors.nublue.co.uk/kafka/2.7.0/kafka_2.13-2.7.0.tgz
		$tar -zxvf kafka_2.13-2.7.0.tgz
		$cd kafka_2.13-2.7.0

#####	2. Update the zookeeper properties in kafka config
		$vi config/zookeeper.properties
		dataDir=<user home dir>/zookeeper/apache-zookeeper-3.6.2-bin/data
		clientPort=2181

#####	3. Start the zookeeper with zookeeper properties in kafka
		$bin/zookeeper-server-start.sh config/zookeeper.properties &

#####	4. Start Kafka broker
		$bin/kafka-server-start.sh config/server.properties &

#####	5. Quick test of Kafka broker by creating topic, producing and consuming messages *(optional)*
		$bin/kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 
		$bin/kafka-topics.sh --list --bootstrap-server localhost:9092
		$bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test-topic
		$bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test-topic --from-beginning
		$bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic test-topic
     

#### 3.	Set-up Kafka Connect with Debezium plugin

#####	1. Install pgoutput plugin with postgres server.
		By default it is available with 10+ postgres version
		
#####	2. Configure postgres and set wal_level for logical decoding with the write-ahead log
		$vi /var/lib/pgsql/10/data/postgresql.conf
	    wal_level = logical
		Restart the postgres server

#####	3. Download debezium postgres connectors plugin and extract.
		$mkdir ~/Debezium
		$cd ~/Debezium
		$wget https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/1.2.0.Final/debezium-connector-postgres-1.2.0.Final-plugin.tar.gz
		$tar -zxvf debezium-connector-postgres-1.2.0.Final-plugin.tar.gz

#####	4. Configure kafka connector properties file, Update below parameter	
		$cd ~/kafka/kafka_2.13-2.7.0/config
		$vi connect-standalone.properties
	
		plugin.path=<user path>/Debezium
	    key.converter.schemas.enable=false
	    value.converter.schemas.enable=false

#####	5. Create the connector directory to keep all connector property files.
		$cd ~/kafka/kafka_2.13-2.7.0
		$mkdir connector
	    
#### 4.	Spark cluster set-up and configuration 

#####	1. Create spark directory, install the latest `2.*` version, and configure
		$mkdir ~/spark
		$cd ~/spark
		$wget https://apache.mirrors.nublue.co.uk/spark/spark-2.4.7/spark-2.4.7-bin-hadoop2.7.tgz
		$tar -xvf spark-2.4.7-bin-hadoop2.7.tgz
		$vi ~/.bashrc
		export SPARK_HOME=~/spark/spark-2.4.7-bin-hadoop2.7
		export PATH=$PATH:$SPARK_HOME/bin
		$source ~/.bashrc
	
#####	2. Start spark cluster (master and slave)
		$cd ~/spark/spark-2.4.7-bin-hadoop2.7
		$./sbin/start-master.sh 
		$curl http://<internal_ip>:8080/  #note master-spark-URL
		$./sbin/start-slave.sh <master-spark-URL>
			
#####	3.Installation of Python2, PIP (the python2 compatible) and Required Packages
		$sudo yum install python2
		$whereis python2
		$sudo ln -s /usr/bin/python2 /usr/bin/python
		$wget https://bootstrap.pypa.io/2.7/get-pip.py 
		$python get-pip.py

		$python -m pip install elasticsearch
		$python -m pip install jproperties
		$python -m pip install kafka-python
		$python -m pip install pyspark
		$python -m pip install configparser

#####	4. Run pyspark shell (optional)
		$./bin/pyspark

#####	5. Run sample python file (optional)
		$./bin/spark-submit example.py

### 4.	Deployment of Reporting framework 

#### 1.	Kafka Deployment

#####	1. Create required kafka topic to stream data from postgres
		$cd ~/kafka/kafka_2.13-2.7.0
		$bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic REPORTING-SERVER.ida.auth_transaction
		
		$bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic REPORTING-SERVER.prereg.applicant_demographic
		
		$bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic REPORTING-SERVER.regprc.registration_list
		
		$bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic REPORTING-SERVER.regprc.registration
		
		$bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic REPORTING-SERVER.audit.app_audit_log

#####	2. Validate all require topics are created as expected using below command.

		$bin/kafka-topics.sh --list --bootstrap-server localhost:9092

#### 2.	Spark streaming Jobs deployment
#####	1. Copy spark python jobs from git repository (`reporting/reporting-framework/data-streaming/jobs/*`) to below directory
		$sudo yum install git
		$cd ~
		$git clone https://github.com/mosip/reporting.git
		$cd reporting
		$git checkout 1.1.4
		$mkdir ~/spark/python-jobs
		$cp ~/reporting/reporting-framework/data-streaming/jobs/* ~/spark/python-jobs/
			
#####	2. Copy spark jobs properties from git repository (`reporting/reporting-framework/data-streaming/properties/*`) to below directory
		$cp ~/reporting/reporting-framework/data-streaming/properties/* ~/spark/spark-2.4.7-bin-hadoop2.7/

#####	3. Update the properties (`appconfig.properties`)file with valid elasticsearch host (internal_ip), port, user credentials and index name details
		$cd ~/spark/spark-2.4.7-bin-hadoop2.7/
		$vi appconfig.properties

#####	4. Run spark streaming jobs using below command

		$./bin/spark-submit --packages org.apache.spark:spark-streaming-kafka-0-8_2.11:2.4.7 ~/spark/python-jobs/mosip-db-streaming-audit.py localhost:9092 REPORTING-SERVER.audit.app_audit_log &
	
		$./bin/spark-submit --packages org.apache.spark:spark-streaming-kafka-0-8_2.11:2.4.7 ~/spark/python-jobs/mosip-db-streaming-ida.py localhost:9092 REPORTING-SERVER.ida.auth_transaction &
	
		$./bin/spark-submit --packages org.apache.spark:spark-streaming-kafka-0-8_2.11:2.4.7 ~/spark/python-jobs/mosip-db-streaming-prereg.py localhost:9092 REPORTING-SERVER.prereg.applicant_demographic &
	
		$./bin/spark-submit --packages org.apache.spark:spark-streaming-kafka-0-8_2.11:2.4.7 ~/spark/python-jobs/mosip-db-streaming-regclient.py localhost:9092 REPORTING-SERVER.regprc.registration_list &
	
		$./bin/spark-submit --packages org.apache.spark:spark-streaming-kafka-0-8_2.11:2.4.7 ~/spark/python-jobs/mosip-db-streaming-reg.py localhost:9092 REPORTING-SERVER.regprc.registration &

#### 3.	Kafka Connect Deployment with Debezium
#####	1. Copy connector properties from git repository (`reporting/reporting-framework/kafka-connect/properties/*`) to below directory

		$cp ~/reporting/reporting-framework/kafka-connect/properties/* ~/kafka/kafka_2.13-2.7.0/connector/

#####	2. Update the connector properties (`~/kafka/kafka_2.13-2.7.0/connector/*.properties`) files with postgres details like, host, port, user credentials, database, schema and table details.~/kaf

#####	3. Start kafka connector with below commands
		$cd ~/kafka/kafka_2.13-2.7.0
		$bin/connect-standalone.sh config/connect-standalone.properties connector/connector-audit.properties connector/connector-ida.properties connector/connector-prereg.properties connector/connector-regclient.properties connector/connector-reg.properties &
			
#### 4.	Deploy Kibana Dashboard and reference Reports
#####	1. Copy reference dashboard and report file from git repository (reporting/reporting-framework/mosip-ref-dashboard/mosip-dashboards.ndjson, mosip-reports.ndjson) to local where you are accessing kibana using browser.

#####	2. Load mosip-dashboards.ndjson, mosip-reports.ndjson on to kibana using below steps
		1.	Login to kibana
		2.	Go to left top menu and select Stack Management
		3.	Select saved objects under kibana section 
		4.	Top right click on Import 
		5.	Select mosip-dashboards.ndjson and click on import
		6.	Select mosip-reports.ndjson and click on import

#####	3. Go to Kibana dashboards and check all the dashboards are visible


#### 5.	Deploy logstash pipelines
#####	1. Copy logstash pipeline configs from repo (reporting/reporting-framework/logstah-config/*) to server where logstash is running to below directory.
			/usr/share/logstash/config-directory -- Create directory if not exist

#####	2. Make required changes in the config files like db connection url, username, password, elastic search host, port...etc.

#####	3. Run logstash pipelines using below command.
			$cd /usr/share/logstash
			$/bin/logstash -f config-directory &
			
#####	4. Check for records are storing on to index in elastic search.	

### 5.	Testing end to end Reporting Framework
#### 1.	Test Postgres server and load data
#####	a.	Load some sample data on to postgres on the tables used for data streaming
#####	b.	Check data is streaming using kafka connector and kafka broaker by consuming data using below command
			$bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic <topic-name> --from-beginning
			
#### 2.	Test Elastic Search Index for data update
#####	a.	Test records number is increased on elastic index using below command
			$curl -u elastic:elastic -X GET "xxx.xx.xx.xx:9200/_cat/indices?v"

#####	b.	Check for below index names
			prereg-indexname = idx-prereg
			regclient-indexname = idx-regclient
			reg-indexname = idx-reg
			ida-indexname = idx-ida
			audit-indexname = idx-audit
#### 3.	Test Kibana dashboards Update
#####	a.	Login to Kibana and check for dashboards update with new records and number of records.
