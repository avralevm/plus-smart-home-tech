package ru.yandex.practicum.service.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.handler.hub.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HubEventServiceImpl implements HubEventService {
    private final Map<String, HubEventHandler> hubEventHandlers;

    public HubEventServiceImpl(Set<HubEventHandler> hubEventHandlers) {
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getType,
                        Function.identity()));
    }

    @Override
    public void handle(HubEventAvro event) {
        String typeHandle = event.getPayload().getClass().getName();
        if (hubEventHandlers.containsKey(typeHandle)) {
            log.info("Processed hub event. Type: {}, Event: {}", event.getPayload(), event);
            hubEventHandlers.get(typeHandle).handle(event);
        } else {
            throw new IllegalArgumentException("Не могу найти обработчик для события " + event.getPayload());
        }
    }
}
