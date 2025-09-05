package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final SensorRepository repository;
    private final HubEventMapper mapper;

    @Override
    public String getType() {
        return DeviceAddedEventAvro.class.getName();
    }

    @Override
    public void handle(HubEventAvro event) {
        DeviceAddedEventAvro eventAvro = (DeviceAddedEventAvro) event.getPayload();
        Optional<Sensor> currentSensor = repository.findByIdAndHubId(eventAvro.getId(), event.getHubId());
        if (currentSensor.isEmpty()) {
            Sensor sensor = mapper.mapToSensor(eventAvro.getId(), event.getHubId());
            log.info("{Сохраняем в БД sensor: {}",  sensor);
            repository.save(sensor);
        } else {
            log.info("Устройство уже существует: {}", currentSensor.get());
        }
    }
}
