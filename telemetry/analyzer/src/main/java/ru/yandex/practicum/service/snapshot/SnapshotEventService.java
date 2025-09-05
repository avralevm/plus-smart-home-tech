package ru.yandex.practicum.service.snapshot;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public interface SnapshotEventService {
    void handle(SensorsSnapshotAvro sensorsSnapshotAvro);
}