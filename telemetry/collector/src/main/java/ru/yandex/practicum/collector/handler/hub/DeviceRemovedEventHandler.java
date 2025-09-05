package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeviceRemovedEventHandler implements HubEventHandler {
    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public HubEventAvro handle(HubEventProto event) {
        DeviceRemovedEventProto eventProto = event.getDeviceRemoved();

        DeviceRemovedEventAvro eventAvro = DeviceRemovedEventAvro.newBuilder()
                .setId(eventProto.getId())
                .build();
        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(), event.getTimestamp().getNanos()))
                .setPayload(eventAvro)
                .build();
    }
}
