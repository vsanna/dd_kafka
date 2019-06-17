package dev.ishikawa.demo.kafka.tutorial2;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    Logger logger = LoggerFactory.getLogger(TwitterProducer.class);

    public TwitterProducer(){}

    public static void main(String[] args) {
        new TwitterProducer().run();
    }


    public void run() {
        logger.info("Setup");

        // create twitter client
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
        Client client = createTwitterClient(msgQueue);
        client.connect();

        // create kafka producer
        final KafkaProducer<String, String> producer = createKafkaProducer();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping application...");
            logger.info("shutting down client from twitter...");
            client.stop();
            logger.info("closing producer...");
            producer.close();
            logger.info("done!");
        }));

        // loop to send tweets to kafka
        while(!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
                producer.send(new ProducerRecord<String, String>("twitter_tweets", msg), new Callback() {
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e != null) {
                            logger.error("Something bad happended", e);
                        }
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }

            if(msg != null) {
                logger.info(msg);
            }

        }
    }

    private KafkaProducer<String, String> createKafkaProducer() {
        Properties property = new Properties();
        property.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        property.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        property.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // safe producer
        property.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        property.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        property.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        property.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");


        // high throughput producer (at the expense of a bit of latency and CPU usage)
        property.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        property.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        property.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));


        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(property);
        return producer;
//
//        // send data
//        ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "from java");
//
//        producer.send(record);
//
//        producer.flush();
//
//        producer.close();
    }

    public Client createTwitterClient(BlockingQueue msgQueue) {
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        // Optional: set up some followings and track terms
        List<Long> followings = Lists.newArrayList(1234L, 566788L);
        List<String> terms = Lists.newArrayList("bitcoin", "usa", "politics", "sport", "soccer");
        hosebirdEndpoint.followings(followings);
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(
                "sfDCilb5rtsI8RHnAUfRiW1dg",
                "9qCe2guP8n7q3pPRYDQo3EO3IZvgNudCdPgIALjpGX6LpNmkCe",
                "2529132853-3Lntd0HJn04hQIfMuLRynP76f0w0VbyW0HqYQnK",
                "ntAs7DJdtR9jloOJV0uMoteSgzSufDhHiiv3PXLlIC7wi"
        );

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));
//                .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

        Client hosebirdClient = builder.build();
        return hosebirdClient;
    }
}
