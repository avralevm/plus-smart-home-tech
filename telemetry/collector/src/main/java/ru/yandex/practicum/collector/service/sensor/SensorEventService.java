package ru.yandex.practicum.collector.service.sensor;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public interface SensorEventService {
    void sendEvent(SensorEventAvro event);
}
