package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public HubEvent handle(HubEventProto event) {
        ScenarioAddedEventProto scenarioAddedEvent = event.getScenarioAdded();

        return ScenarioAddedEvent.builder()
                .hubId(event.getHubId())
                .timestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()))
                .name(scenarioAddedEvent.getName())
                .conditions(scenarioAddedEvent.getConditionList().stream()
                        .map(ScenarioAddedEventHandler::handleScenarioCondition)
                        .toList())
                .actions(scenarioAddedEvent.getActionList().stream()
                        .map(ScenarioAddedEventHandler::handleDeviceAction)
                        .toList())
                .build();
    }

    private static ScenarioCondition handleScenarioCondition(ScenarioConditionProto condition) {
        return ScenarioCondition.builder()
                .sensorId(condition.getSensorId())
                .type(ConditionType.valueOf(condition.getType().toString()))
                .operation(ConditionOperation.valueOf(condition.getOperation().toString()))
                .value(condition.getIntValue())
                .build();
    }

    private static DeviceAction handleDeviceAction(DeviceActionProto deviceAction) {
        return DeviceAction.builder()
                .sensorId(deviceAction.getSensorId())
                .type(ActionType.valueOf(deviceAction.getType().toString()))
                .value(deviceAction.getValue())
                .build();
    }
}
