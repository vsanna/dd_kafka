## setup

```bash
## install & run
### 1. via docker
$ git clone https://github.com/wurstmeister/kafka-docker
$ cd kafka-docker

#### edit docker-compose single-broker.yml
$ vi docker-compose-single-broker.yml

- KAFKA_ADVERTISED_HOST_NAME: 192.168.99.100
+ KAFKA_ADVERTISED_HOST_NAME: 127.0.0.01

#### docker-compose up
$ docker-compose -f docker-compose docker-compose-single-broker.yml up -d


### 2. via binary(I suggest this way)
#### visit https://kafka.apache.org/downloads
#### select newest binary (i.g. https://www.apache.org/dyn/closer.cgi?path=/kafka/2.2.1/kafka_2.12-2.2.1.tgz)
#### download
#### add a new path to PATH
#### edit config
##### 1. path/to/kafka/config/server.properties
- num.pertitions = 1
- advertised.host.name = xxxxxxxxx 
+ num.pertitions = 3
+ advertised.host.name = localhost
+ log.dirs = path/to/kafka/data/kafka
+ default.replication.factor = 3

##### 2. path/to/kafka/config/zookeeper.properties
dataDir=path/to/kafka/data/zookeeper

#### run
$ zookeeper-server-start path/to/kafka/config/zookeeper.properties
$ kafka-server-start path/to/kafka/config/server.properties

### 3. via brew
$ brew install kafka


## install kafka clients
$ brew cask install java
$ brew install kafka
```

## use
```Makefile
kafka_port := 9092
zookeeper_port := 2181

zookeeper_option := --zookeeper localhost:$(zookeeper_port)
topic := hello_world
topic_option := --topic $(topic)

config_path := /Users/xxxxx/kafka/config

# ===========================
# Server
# ===========================
kafka-start:
	kafka-server-start $(config_path)/server.properties

zookeeper-start:
	zookeeper-server-start $(config_path)/zookeeper.properties

start: zookeeper-start kafka-start

stop:
	kafka-server-stop
	zookeeper-server-stop


# ===========================
# Topic
# ===========================
create-topic:
	kafka-topics --create --replication-factor 1 --partitions 1 $(zookeeper_option) $(topic_option)

index-topic:
	kafka-topics --list $(zookeeper_option)

show-topic:
	kafka-topics --describe $(zookeeper_option) $(topic_option)

delete-topic:
	kafka-topics --delete $(zookeeper_option) $(topic_option)


# ===========================
# Consumer / Producer 
# ===========================
start-consumer:
	kafka-console-consumer --bootstrap-server localhost:$(kafka_port) $(topic_option) --from-beginning

start-producer:
	kafka-console-producer --broker-list localhost:$(kafka_port) $(topic_option)
```
