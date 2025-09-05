package ru.yandex.practicum.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.model.ConditionType;

import static ru.yandex.practicum.model.ConditionType.MOTION;

@Component
public class MotionSensorHandler implements SensorEventHandler {
    @Override
    public String getType() {
        return MotionSensorHandler.class.getName();
    }

    @Override
    public Integer handleToValue(SensorStateAvro stateAvro, ConditionType type) {
        MotionSensorAvro sensorAvro = (MotionSensorAvro) stateAvro.getData();

        if (type == MOTION) {
            return sensorAvro.getMotion() ? 1 : 0;
        }

        return null;
    }
}
