package ru.yandex.practicum.collector.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
public class HubEventMapper {
    public HubEventAvro toHubEventAvro(HubEvent event) {
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(mapPayload(event))
                .build();
    }

    private SpecificRecordBase mapPayload(HubEvent event) {
        return switch (event.getType()) {
            case DEVICE_ADDED -> mapDeviceAddedEvent((DeviceAddedEvent) event);
            case DEVICE_REMOVED -> mapDeviceRemovedEvent((DeviceRemovedEvent) event);
            case SCENARIO_ADDED -> mapScenarioAddedEvent((ScenarioAddedEvent) event);
            case SCENARIO_REMOVED -> mapScenarioRemovedEvent((ScenarioRemovedEvent) event);
        };
    }

    private DeviceAddedEventAvro mapDeviceAddedEvent(DeviceAddedEvent event) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(DeviceTypeAvro.valueOf(event.getDeviceType().name()))
                .build();
    }

    private DeviceRemovedEventAvro mapDeviceRemovedEvent(DeviceRemovedEvent event) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();
    }

    private ScenarioRemovedEventAvro mapScenarioRemovedEvent(ScenarioRemovedEvent event) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();
    }

    private ScenarioAddedEventAvro mapScenarioAddedEvent(ScenarioAddedEvent event) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(event.getConditions()
                        .stream()
                        .map(HubEventMapper::mapScenarioCondition)
                        .toList())
                .setActions(event.getActions()
                        .stream()
                        .map(HubEventMapper::mapDeviceAction)
                        .toList())
                .build();
    }

    private static ScenarioConditionAvro mapScenarioCondition(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue())
                .build();
    }

    private static DeviceActionAvro mapDeviceAction(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
