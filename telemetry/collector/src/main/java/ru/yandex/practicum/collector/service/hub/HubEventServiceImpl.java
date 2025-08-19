package ru.yandex.practicum.collector.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.mapper.HubEventMapper;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubEventServiceImpl implements HubEventService{
    private final Producer<String, SpecificRecordBase> producer;
    private final HubEventMapper mapper;

    @Value("${collector.kafka.producer.topics.hub-events}")
    private String topic;

    @Override
    public void sendEvent(HubEvent event) {
        HubEventAvro message = mapper.toHubEventAvro(event);

        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                event.getHubId(),
                message);

        log.info("Send record: {} \n", record);
        producer.send(record);
    }
}
