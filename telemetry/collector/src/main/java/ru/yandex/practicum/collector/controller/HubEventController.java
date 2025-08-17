package ru.yandex.practicum.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.service.hub.HubEventService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/events/hubs")
public class HubEventController {
    private final HubEventService service;

    @PostMapping
    public void collectHubEvent(@RequestBody @Valid HubEvent event) {
        log.info("\n[POST] Body: {} \n", event);
        service.sendEvent(event);
    }
}