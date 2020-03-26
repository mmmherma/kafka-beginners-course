package com.github.kafka.tutorial3;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
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
import java.util.Collections;
import java.util.Properties;

public class ElasticSearchConsumer {
    public static void main(String[] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
        RestHighLevelClient client = createClient();

        KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets");

        // STEP 4. Poll for new data
        while(true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for(ConsumerRecord<String, String> record : records) {
                    // Insert data into ElasticSearch
                    String jsonString = record.value();

                    IndexRequest indexRequest = new IndexRequest(
                            "twitter",
                            "tweets"
                    ).source(jsonString, XContentType.JSON);

                    IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
                    String id = indexResponse.getId();
                    logger.info(id);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (ElasticsearchStatusException e) {
                logger.error("ElasticSearchException: " + e);
            }

        }

        //client.close();
    }

    public static KafkaConsumer<String, String> createConsumer(String topic) {
        // Variables
        String bootstrapServers = "kafka-server1:9092";
        String groupId = "kafka-demo-elasticsearch";
        String offsetReset = "earliest"; // earliest/latest/node

        // STEP 1. Create consumer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset);

        // STEP 2. Create the consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        // STEP 3. Subscribe/Consume our topic(s)
        consumer.subscribe(Arrays.asList(topic));

        return consumer;
    }

    public static RestHighLevelClient createClient() {
        // Replace with your own credentials
        String hostname = "kafka-testing-7931713306.eu-west-1.bonsaisearch.net";
        String username = "7fcomgy782";
        String password = "j7366s6rze";

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

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
}
