package ru.yandex.practicum.collector.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubEventServiceImpl implements HubEventService{
    private final Producer<String, SpecificRecordBase> producer;

    @Value("${collector.kafka.producer.topics.hub-events}")
    private String topic;

    @Override
    public void sendEvent(HubEventAvro event) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                event.getHubId(),
                event);

        log.info("Send record: {} \n", record);
        producer.send(record);
    }
}
