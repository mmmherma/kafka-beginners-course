package kafka.tutorial1;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerDemoSafe {
    public static void main(String[] args) {
        String bootstrapServers = "kafka-server1:9092";

        // STEP 1. Create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Create safe producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // Due to Kafka > 1.1

        // STEP 2. Create te producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        // STEP 3. Send data
        // Create a ProducerRecord
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "hello world!");
        // Send data -- This is asynchronous
        producer.send(record);
        // Flush messages to send them
        producer.flush();
        // Close producer
        producer.close();
    }
}
