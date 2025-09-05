package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import com.sun.jdi.request.StepRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Scenario;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class HubRouterClientService {
    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public void sendScenarioActions(List<Scenario> scenarios) {
        for (Scenario scenario : scenarios) {
            sendScenarioActions(scenario);
        }
    }

    private void sendScenarioActions(Scenario scenario) {
        for (Map.Entry<String, Action> entry : scenario.getActions().entrySet()) {
            String sensorId = entry.getKey();
            Action action = entry.getValue();

            DeviceActionRequest request = mapToActionRequest(scenario, sensorId, action);
            try {
                hubRouterClient.handleDeviceAction(request);
                log.info("Отправлена команда для сценария {} на датчик {}", scenario.getName(), sensorId);
            } catch (Exception e) {
                log.error("Ошибка при отправке команды для сценария {}", scenario.getName(), e);
            }
        }
    }

    private DeviceActionRequest mapToActionRequest(Scenario scenario, String sensorId, Action action) {
        return DeviceActionRequest.newBuilder()
                .setHubId(scenario.getHubId())
                .setScenarioName(scenario.getName())
                .setAction(mapToActionProto(sensorId, action))
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano()))
                .build();
    }

    private DeviceActionProto mapToActionProto(String sensorId, Action action) {
        return DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
