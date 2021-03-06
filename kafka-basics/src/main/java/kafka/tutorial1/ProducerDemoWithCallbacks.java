package kafka.tutorial1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallbacks {
    public static void main(String[] args) {
        // Create Logger
        final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallbacks.class);

        // Variables
        String bootstrapServers = "kafka-server1:9092";

        // STEP 1. Create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // STEP 2. Create te producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        // STEP 3. Send data
        for (int i = 0; i < 10; i++) {
            // Create a ProducerRecord
            ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "hello world!" + Integer.toString(i));
            // Send data -- This is asynchronous
            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    // Executes when record is successfylly sent or an exception is thrown
                    if(e == null) {
                        // Record successfully sent
                        logger.info("Received new metadata:");
                        logger.info("Topic: " + recordMetadata.topic());
                        logger.info("Partition: " + recordMetadata.partition());
                        logger.info("Offset: " + recordMetadata.offset());
                        logger.info("Timestamp: " + recordMetadata.timestamp());
                    } else {
                        logger.error("Error produced: " + e);
                    }
                }
            });
        }

        // Flush messages to send them
        producer.flush();
        // Close producer
        producer.close();
    }
}
