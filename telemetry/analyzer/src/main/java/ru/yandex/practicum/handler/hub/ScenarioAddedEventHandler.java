package ru.yandex.practicum.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final ScenarioRepository repository;
    private final HubEventMapper mapper;

    @Override
    public String getType() {
        return ScenarioAddedEventAvro.class.getName();
    }

    @Override
    public void handle(HubEventAvro event) {
        ScenarioAddedEventAvro eventAvro = (ScenarioAddedEventAvro) event.getPayload();
        Optional<Scenario> currentScenario = repository.findByHubIdAndName(event.getHubId(), eventAvro.getName());
        if (currentScenario.isEmpty()) {
            Scenario scenario = mapper.mapToScenario(event, eventAvro);
            log.info("{Сохраняем в БД scenario: {}",  scenario);
            repository.save(scenario);
        } else {
            Scenario scenario = currentScenario.get();
            log.info("Устройство уже существует: id={}, hubId={}, name={}",
                    scenario.getId(), scenario.getHubId(), scenario.getName());
        }
    }
}
