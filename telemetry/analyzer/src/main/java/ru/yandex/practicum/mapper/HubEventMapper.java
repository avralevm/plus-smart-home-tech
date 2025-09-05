package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.model.*;

import java.util.stream.Collectors;

@Component
public class HubEventMapper {
    public Sensor mapToSensor(String id, String hubId) {
        return Sensor.builder()
                .id(id)
                .hubId(hubId)
                .build();
    }

    public Scenario mapToScenario(HubEventAvro hubEvent, ScenarioAddedEventAvro scenarioEvent) {
        return Scenario.builder()
                .hubId(hubEvent.getHubId())
                .name(scenarioEvent.getName())
                .conditions(scenarioEvent.getConditions().stream()
                        .collect(Collectors.toMap(ScenarioConditionAvro::getSensorId, HubEventMapper::mapToCondition)))
                .actions(scenarioEvent.getActions().stream()
                        .collect(Collectors.toMap(DeviceActionAvro::getSensorId, HubEventMapper::mapToAction)))
                .build();
    }

    private static Action mapToAction(DeviceActionAvro action) {
        return Action.builder()
                .type(ActionType.valueOf(action.getType().name()))
                .value(action.getValue())
                .build();
    }

    private static Condition mapToCondition(ScenarioConditionAvro condition) {
        return Condition.builder()
                .type(ConditionType.valueOf(condition.getType().name()))
                .operation(ConditionOperation.valueOf(condition.getOperation().name()))
                .value(mapValue(condition.getValue()))

                .build();
    }

    private static Integer mapValue(Object conditionValue) {
        return switch (conditionValue) {
            case null -> null;
            case Boolean b -> (b ? 1 : 0);
            case Integer i -> i;
            default -> throw new ClassCastException("Ошибка преобразования значения");
        };
    }
}
