package com.github.vsanna.kafka.tutorial1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {

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


        for (int i = 0; i < 10; i++) {
            String topic = "first_topic";
            String value = "hello world" + Integer.toString(i);
            String key = "id_" + Integer.toString(i);

            // send data
            final ProducerRecord<String, String> record
                    = new ProducerRecord<String, String>(topic, key, value);

            logger.info("Key: " + key);

            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    // execute every time a record is successfully sent or an exception is thrown

                    if (e == null) {
                        // success
                        logger.info(("Received new metadata: \n" +
                                "Topic: " + recordMetadata.topic() + "\n" +
                                "Partition: " + recordMetadata.partition() + "\n" +
                                "Offset: " + recordMetadata.offset() + "\n" +
                                "Timestamp: " + recordMetadata.timestamp()));
                    } else {
                        // failure
                        logger.error("Error while production", e);
                    }
                }
            }); // block the .send() to make it synchronous ; don't do this on production
        }

        producer.flush();
        producer.close();
    }
}
