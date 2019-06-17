package dev.ishikawa.demo.kafka.turorial3;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ElasticSearchConsumer {


    public static RestHighLevelClient createClient() {
        // https://q2zmnx35yj:69uxwxmaa0@kafka-course-1932826160.ap-southeast-2.bonsaisearch.net:443
        String hostname = "kafka-course-1932826160.ap-southeast-2.bonsaisearch.net";
        String username = "q2zmnx35yj";
        String password = "69uxwxmaa0";

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        RestClientBuilder builder = RestClient.builder(
            new HttpHost(hostname, 443, "https")
        ).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        });

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }

    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
        RestHighLevelClient client = createClient();

        KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets");

        while(true) {
            ConsumerRecords<String, String> records
                    = consumer.poll(Duration.ofMillis(100));

            logger.info("Received: " + records.count() + " records");

            BulkRequest bulkRequest = new BulkRequest();

            for (var record: records) {
                // 2 strategies to create id
                //   1. kafka generic ID
                // String id = record.topic() + "_" + record.partition() + "_" + record.offset();
                //   2.twitter feed specific id
                String id = extractIdFromTweet(record.value());

                String tweet = record.value();
                IndexRequest indexRequest = new IndexRequest(
                        "twitter",
                        "tweets",
                        id // this is to make our consumer idempotent
                ).source(tweet, XContentType.JSON);

                bulkRequest.add(indexRequest);

//                IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
//                logger.info(indexResponse.getId());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(records.count() > 0) {
                BulkResponse bulkItemResponses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                logger.info(
                        String.valueOf(Arrays.stream(bulkItemResponses.getItems())
                                .map((item) -> { return item.getId(); }))
                );

                logger.info("Committing offsets...");
                consumer.commitSync();
                logger.info("offsets have been committed");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

//        client.close();
    }

    private static JsonParser jsonParser = new JsonParser();

    private static String extractIdFromTweet(String value) {
        return jsonParser.parse(value).getAsJsonObject()
                .get("id_str")
                .getAsString();
    }

    public static KafkaConsumer<String, String> createConsumer(String topic) {
        String groupId = "kafka-demo-elasticsearch";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");

        KafkaConsumer<String, String> consumer =
                new KafkaConsumer<String, String>(properties);

        consumer.subscribe(Arrays.asList(topic));
        return consumer;

    }
}
