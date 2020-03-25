package com.github.simplekafka.kafka.tutorial1;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.java2d.SurfaceDataProxy;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThreads {
    public static void main(String[] args) {
        new ConsumerDemoWithThreads().run();
    }

    private ConsumerDemoWithThreads() {

    }

    private void run() {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class.getName());

        // Variables
        String bootstrapServers = "kafka-server1:9092";
        String groupId = "my-sixth-application";
        String topic = "first_topic";

        // Latch for dealing with multiple threads
        CountDownLatch latch = new CountDownLatch(1);

        // Creates consumer runnable
        Runnable myConsumerRunnable = new ConsumerRunnable(
                bootstrapServers,
                groupId,
                topic,
                latch
        );

        // Start thread
        Thread myThread = new Thread(myConsumerRunnable);
        myThread.start();

        // Add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            logger.info("Caught shutdown hook");
            ((ConsumerRunnable) myConsumerRunnable).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application exited");
        }
        ));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application interrupted");
        } finally {
            logger.info("Application is closing");
        }
    }

    public class ConsumerRunnable implements Runnable {
        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private String offsetReset;

        private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class.getName());

        public ConsumerRunnable(String bootstrapServers,
                              String groupId,
                              String topic,
                              CountDownLatch latch) {
            this.latch = latch;
            this.offsetReset = "earliest"; // earliest/latest/node

            // STEP 1. Create consumer properties
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.offsetReset);

            // STEP 2. Create the consumer
            this.consumer = new KafkaConsumer<String, String>(properties);

            // STEP 3. Subscribe/Consume our topic(s)
            this.consumer.subscribe(Collections.singleton(topic));
        }

        @Override
        public void run() {
            // STEP 4. Poll for new data
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key: " + record.key());
                        logger.info("Value: " + record.value());
                        logger.info("Partition: " + record.partition());
                        logger.info("Offset: " + record.offset());
                    }
                }
            } catch (WakeupException e) {
                this.logger.info("Received shutdown signal");
            } finally {
                consumer.close();
                // Tell main code consumer is done
                latch.countDown();
            }
        }

        public void shutdown() {
            // wakeup() is a special method to interrupy consumer.poll()
            // Throws a WakeUpException
            consumer.wakeup();
        }
    }
}
