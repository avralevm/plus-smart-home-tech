package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationServiceImpl implements AggregationService {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();
    @Value("${collector.kafka.topics.snapshot-events}")
    private String topic;

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro currentSnapshot = snapshots.getOrDefault(
                event.getHubId(),
                createNewSensorsSnapshot(event.getHubId(), event.getTimestamp()));

        SensorStateAvro oldState = currentSnapshot.getSensorsState().get(event.getId());
        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(event.getTimestamp()) || oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = createSensorState(event);
        currentSnapshot.getSensorsState().put(event.getId(), newState);
        currentSnapshot.setTimestamp(event.getTimestamp());
        snapshots.put(event.getHubId(), currentSnapshot);

        return Optional.of(currentSnapshot);
    }

    @Override
    public void sendSnapshot(Producer<String, SpecificRecordBase> producer, SensorsSnapshotAvro snapshot) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                snapshot.getHubId(),
                snapshot);

        log.info("Send record: {} \n", record);
        producer.send(record);
    }

    private SensorsSnapshotAvro createNewSensorsSnapshot(String hubId, Instant timestamp) {
        return SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(timestamp)
                .setSensorsState(new HashMap<>())
                .build();
    }

    private SensorStateAvro createSensorState(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
    }
}