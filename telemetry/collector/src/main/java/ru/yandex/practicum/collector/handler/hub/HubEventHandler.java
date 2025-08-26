package ru.yandex.practicum.collector.handler.hub;

import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubEventHandler {
    HubEventProto.PayloadCase getMessageType();

    HubEvent handle(HubEventProto event);
}
