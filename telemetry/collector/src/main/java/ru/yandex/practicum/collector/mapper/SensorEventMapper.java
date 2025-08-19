package ru.yandex.practicum.collector.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
public class SensorEventMapper {
    public SensorEventAvro toSensorEventAvro(SensorEvent event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(mapPayload(event))
                .build();
    }

    private SpecificRecordBase mapPayload(SensorEvent event) {
        return switch (event.getType()) {
            case MOTION_SENSOR_EVENT -> mapMotionSensorEvent((MotionSensorEvent) event);
            case TEMPERATURE_SENSOR_EVENT -> mapTemperatureSensorEvent((TemperatureSensorEvent) event);
            case LIGHT_SENSOR_EVENT -> mapLightSensorEvent((LightSensorEvent) event);
            case CLIMATE_SENSOR_EVENT -> mapClimateSensorEvent((ClimateSensorEvent) event);
            case SWITCH_SENSOR_EVENT -> mapSwitchSensorEvent((SwitchSensorEvent) event);
        };
    }

    private MotionSensorAvro mapMotionSensorEvent(MotionSensorEvent event) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setMotion(event.isMotion())
                .setVoltage(event.getVoltage())
                .build();
    }

    private TemperatureSensorAvro mapTemperatureSensorEvent(TemperatureSensorEvent event) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();
    }

    private LightSensorAvro mapLightSensorEvent(LightSensorEvent event) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
    }

    private ClimateSensorAvro mapClimateSensorEvent(ClimateSensorEvent event) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setCo2Level(event.getCo2Level())
                .setHumidity(event.getHumidity())
                .build();
    }

    private SwitchSensorAvro mapSwitchSensorEvent(SwitchSensorEvent event) {
        return SwitchSensorAvro.newBuilder()
                .setState(event.isState())
                .build();
    }
}