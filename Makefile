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
