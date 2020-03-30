package kafka.tutorial2;

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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {
    Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());
    List<String> terms = Lists.newArrayList("kafka");

    public TwitterProducer() {

    }

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        // Create Twitter client
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

        Client client = createTwitterClient(msgQueue);
        // Attempts to establish a connection.
        client.connect();

        // Create Kafka producer
        KafkaProducer<String, String> producer = createKafkaProducer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping apllication");

            logger.info("Closing Twitter client");
            client.stop();
            logger.info("Closing Kafka producer");
            producer.close();

            logger.info("Done");
        }));

        // Loop to send tweets to Kafka
        // on a different thread, or multiple different threads....
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }

            if(msg != null) {
                producer.send(new ProducerRecord<>("twitter_tweets", null, msg), new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if(e != null) {
                            logger.error("Something happend");
                        }
                    }
                });
            }
        }
    }

    public String getConfigParamenter(String key) {
        Properties configProperties = new Properties();
        try {
            String propertiesFile = "config.properties";

            InputStream configStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);

            if(configStream != null) {
                configProperties.load(configStream);
            } else {
                configStream.close();
                throw new FileNotFoundException("Property file: " + propertiesFile + " not found");
            }
        } catch(Exception e) {
            logger.error(String.valueOf(e));
        }

        return configProperties.getProperty(key);
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue) {
        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(
                getConfigParamenter("apiKey"),
                getConfigParamenter("apiSecret"),
                getConfigParamenter("token"),
                getConfigParamenter("tokenSecret")
        );

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();

        return hosebirdClient;
    }

    public KafkaProducer<String, String> createKafkaProducer() {
        String bootstrapServers = "kafka-server1:9092";

        // STEP 1. Create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // STEP 2. Create te producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        /*
        // STEP 3. Send data
        // Create a ProducerRecord
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "hello world!");
        // Send data -- This is asynchronous
        producer.send(record);
        // Flush messages to send them
        producer.flush();
        // Close producer
        producer.close();
        */

        return producer;
    }
}
