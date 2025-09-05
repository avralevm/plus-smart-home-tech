package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public HubEventAvro handle(HubEventProto event) {
        ScenarioAddedEventProto eventProto = event.getScenarioAdded();

        ScenarioAddedEventAvro eventAvro = ScenarioAddedEventAvro.newBuilder()
                .setName(eventProto.getName())
                .setActions(eventProto.getActionList()
                        .stream()
                        .map(ScenarioAddedEventHandler::handleDeviceAction)
                        .toList())
                .setConditions(eventProto.getConditionList()
                        .stream()
                        .map(ScenarioAddedEventHandler::handleScenarioCondition)
                        .toList())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()))
                .setPayload(eventAvro)
                .build();
    }

    private static DeviceActionAvro handleDeviceAction(DeviceActionProto deviceActionProto) {
        return DeviceActionAvro.newBuilder()
                .setType(ActionTypeAvro.valueOf(deviceActionProto.getType().name()))
                .setSensorId(deviceActionProto.getSensorId())
                .setValue(deviceActionProto.getValue())
                .build();
    }

    private static ScenarioConditionAvro handleScenarioCondition(ScenarioConditionProto conditionProto) {
        Object value = switch (conditionProto.getValueCase()) {
            case INT_VALUE -> conditionProto.getIntValue();
            case BOOL_VALUE -> conditionProto.getBoolValue();
            case VALUE_NOT_SET -> throw new IllegalArgumentException("Condition value not set");
        };

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(conditionProto.getSensorId())
                .setOperation(ConditionOperationAvro.valueOf(conditionProto.getOperation().name()))
                .setType(ConditionTypeAvro.valueOf(conditionProto.getType().name()))
                .setValue(value)
                .build();
    }
}
