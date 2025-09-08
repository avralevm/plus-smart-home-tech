package ru.yandex.practicum.handler.sensor;

import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.model.ConditionType;

public interface SensorEventHandler {
    String getType();

    Integer handleToValue(SensorStateAvro stateAvro, ConditionType type);
}
