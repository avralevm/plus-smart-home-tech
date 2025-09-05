package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {
    private final SensorRepository repository;

    @Override
    public String getType() {
        return DeviceRemovedEventAvro.class.getName();
    }

    @Override
    public void handle(HubEventAvro event) {
        DeviceRemovedEventAvro eventAvro = (DeviceRemovedEventAvro) event.getPayload();
        String id = eventAvro.getId();

        if (repository.existsByIdInAndHubId(List.of(id), event.getHubId())) {
            repository.deleteById(id);
            log.info("Sensor c id={}, был удалён", id);
        } else {
            log.info("Sensor c id={}, не был найден", id);
        }
    }
}
