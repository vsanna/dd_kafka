package com.github.vsanna.kafka.tutorial1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {
    static final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class);
    public static void main(String[] args) {
        // create producer properties
        // https://kafka.apache.org/documentation/#producerconfigs
        Properties property = new Properties();

        property.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        // 文字列で指定してもいい
        // property.setProperty("bootstrap.servers", "127.0.0.1:9092");

        // kafka は送ったものを全てbyteにするが、いろいろなシリアライザーもある
        property.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        property.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        // create the producer
        // Key: String, Value: String
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(property);

        // send data
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "from java");

        producer.send(record);

        producer.flush();

        producer.close();
    }
}
