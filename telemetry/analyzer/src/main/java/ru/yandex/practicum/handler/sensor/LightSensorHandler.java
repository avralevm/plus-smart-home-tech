package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.model.ConditionType;

import static ru.yandex.practicum.model.ConditionType.LUMINOSITY;

@Component
public class LightSensorHandler implements SensorEventHandler {

    @Override
    public String getType() {
        return LightSensorHandler.class.getName();
    }

    @Override
    public Integer handleToValue(SensorStateAvro stateAvro, ConditionType type) {
        LightSensorAvro sensorAvro = (LightSensorAvro) stateAvro.getData();

        if (type == LUMINOSITY) {
            return sensorAvro.getLuminosity();
        }

        return null;
    }
}
