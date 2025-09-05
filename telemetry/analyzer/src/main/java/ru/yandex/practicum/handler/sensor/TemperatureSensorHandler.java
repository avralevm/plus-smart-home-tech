package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.model.ConditionType;

import static ru.yandex.practicum.model.ConditionType.TEMPERATURE;

@Component
public class TemperatureSensorHandler implements SensorEventHandler {
    @Override
    public String getType() {
        return TemperatureSensorAvro.class.getName();
    }

    @Override
    public Integer handleToValue(SensorStateAvro stateAvro, ConditionType type) {
        TemperatureSensorAvro sensorAvro = (TemperatureSensorAvro) stateAvro.getData();

        if (type == TEMPERATURE) {
            sensorAvro.getTemperatureC();
        }

        return null;
    }
}
