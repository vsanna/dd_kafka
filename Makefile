ifeq ($(TOPIC),)
	topic := hello_world
else
	topic := $(TOPIC)
endif

kafka_port := 9092
zookeeper_port := 2181

zookeeper_option := --zookeeper localhost:$(zookeeper_port)
topic_option := --topic $(topic)

# ===========================
# Topic
# ===========================

createTopic:
	kafka-topics --create --replication-factor 1 --partitions 1 $(zookeeper_option) $(topic_option)

indexTopic:
	kafka-topics --list $(zookeeper_option)

showTopic:
	kafka-topics --describe $(zookeeper_option) $(topic_option)

deleteTopic:
	kafka-topics --delete $(zookeeper_option) $(topic_option)


# ===========================
# Consumer / Producer 
# ===========================

startConsumer:
	kafka-console-consumer --bootstrap-server localhost:$(kafka_port) $(topic_option) --from-beginning

startProducer:
	kafka-console-producer --broker-list localhost:$(kafka_port) $(topic_option)
