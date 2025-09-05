package ru.yandex.practicum.service.snapshot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.handler.sensor.SensorEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;
import jakarta.transaction.Transactional;
import ru.yandex.practicum.service.HubRouterClientService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SnapshotEventServiceImpl implements SnapshotEventService {
    private final ScenarioRepository repository;
    private final HubRouterClientService hubRouterClient;
    private final Map<String, SensorEventHandler> handlers;

    public SnapshotEventServiceImpl(ScenarioRepository repository,
                                    Set<SensorEventHandler> handlers,
                                    HubRouterClientService hubRouterClient) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getType,
                        Function.identity()));
        this.repository = repository;
        this.hubRouterClient = hubRouterClient;
    }

    @Transactional
    @Override
    public void handle(SensorsSnapshotAvro snapshotAvro) {
        List<Scenario> scenarios = repository.findByHubId(snapshotAvro.getHubId());

        log.info("Получен список Scenario, hubId: {}, : [{}]", snapshotAvro.getHubId(), scenarios);
        List<Scenario> validScenarios = scenarios.stream()
                .filter(scenario -> validateConditions(scenario, snapshotAvro))
                .toList();

        hubRouterClient.sendScenarioActions(validScenarios);
    }

    private Boolean validateConditions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        Map<String, Condition> conditions = scenario.getConditions();
        Map<String, SensorStateAvro> sensorStates = snapshot.getSensorsState();

        if (snapshot == null || snapshot.getSensorsState().isEmpty()) {
            return false;
        }

        return conditions.keySet().stream()
                .allMatch(sensorId -> checkCondition(conditions.get(sensorId), sensorStates.get(sensorId)));
    }

    private boolean checkCondition(Condition condition, SensorStateAvro sensorState) {
        if (sensorState == null) {
            return false;
        }

        String typeHandle = sensorState.getData().getClass().getName();
        if (!handlers.containsKey(typeHandle)) {
            throw new IllegalArgumentException("Не найден данный обработчик: " + typeHandle);
        }

        Integer value = handlers.get(typeHandle).handleToValue(sensorState, condition.getType());
        if (value == null) {
            return false;
        }

        return switch (condition.getOperation()) {
            case EQUALS -> value.equals(condition.getValue());
            case GREATER_THAN -> value > condition.getValue();
            case LOWER_THAN -> value < condition.getValue();
        };
    }
}
