package ru.yandex.practicum.collector.service.hub;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventService {
    void sendEvent(HubEventAvro event);
}