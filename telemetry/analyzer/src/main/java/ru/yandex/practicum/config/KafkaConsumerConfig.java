package ru.yandex.practicum.config;

import deserializer.HubEventDeserializer;
import deserializer.SensorsSnapshotDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    @Value("${collector.kafka.properties.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${collector.kafka.hub.group-id}")
    private String hubGroupId;
    @Value("${collector.kafka.snapshot.group-id}")
    private String snapshotGroupId;

    @Bean(destroyMethod = "close")
    public Consumer<String, SensorsSnapshotAvro> snapshotConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, snapshotGroupId);
        return new KafkaConsumer<>(configProps);
    }

    @Bean(destroyMethod = "close")
    public Consumer<String, HubEventAvro> hubConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, hubGroupId);
        return new KafkaConsumer<>(configProps);
    }
}
