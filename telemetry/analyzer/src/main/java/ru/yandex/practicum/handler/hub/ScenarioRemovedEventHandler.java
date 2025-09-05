package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {
    private final ScenarioRepository repository;

    @Override
    public String getType() {
        return ScenarioRemovedEventAvro.class.getName();
    }

    @Override
    public void handle(HubEventAvro event) {
        ScenarioRemovedEventAvro eventAvro = (ScenarioRemovedEventAvro) event.getPayload();
        Optional<Scenario> currentScenario = repository.findByHubIdAndName(event.getHubId(), eventAvro.getName());
        if (currentScenario.isPresent()) {
            repository.deleteByHubIdAndName(event.getHubId(), eventAvro.getName());
            log.info("Scenario c id={}, был удалён", currentScenario.get());
        } else {
            log.info("Scenario c name={} и hubId={} не был найден", eventAvro.getName(), event.getHubId());
        }
    }
}
