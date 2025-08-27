package ru.yandex.practicum.collector.service.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorEventServiceImpl implements SensorEventService {
    private final Producer<String, SpecificRecordBase> producer;

    @Value("${collector.kafka.producer.topics.sensor-events}")
    private String topic;

    @Override
    public void sendEvent(SensorEventAvro event) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                event.getId(),
                event);

        log.info("Send record: {} \n", record);
        producer.send(record);
    }
}