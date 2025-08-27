package ru.yandex.practicum.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.collector.model.sensor.TemperatureSensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TemperatureSensorHandler implements SensorEventHandler {
    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public SensorEvent handle(SensorEventProto event) {
        TemperatureSensorProto temperatureSensor = event.getTemperatureSensor();

        return TemperatureSensorEvent.builder()
                .id(event.getId())
                .hubId(event.getHubId())
                .timestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()))
                .temperatureC(temperatureSensor.getTemperatureC())
                .temperatureF(temperatureSensor.getTemperatureF())
                .build();
    }
}
