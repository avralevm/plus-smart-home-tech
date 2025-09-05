package ru.yandex.practicum.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Optional;

public interface AggregationService {
    Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event);

    void sendSnapshot(Producer<String, SpecificRecordBase> producer, SensorsSnapshotAvro snapshot);
}
