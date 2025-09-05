package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.model.ConditionType;

import static ru.yandex.practicum.model.ConditionType.SWITCH;

@Component
public class SwitchSensorHandler implements SensorEventHandler {
    @Override
    public String getType() {
        return SwitchSensorAvro.class.getName();
    }

    @Override
    public Integer handleToValue(SensorStateAvro stateAvro, ConditionType type) {
        SwitchSensorAvro sensorAvro = (SwitchSensorAvro) stateAvro.getData();

        if (type == SWITCH) {
            return sensorAvro.getState() ? 1 : 0;
        }

        return null;
    }
}
