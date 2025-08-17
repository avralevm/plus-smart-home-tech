package ru.yandex.practicum.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.collector.service.sensor.SensorEventService;

@RestController
@Slf4j
@RequestMapping("/events/sensors")
@RequiredArgsConstructor
public class SensorEventController {
    private final SensorEventService service;

    @PostMapping
    public void collectSensorEvent(@RequestBody @Valid SensorEvent event) {
        log.info("\n[POST] Body: {} \n", event);
        service.sendEvent(event);
    }
}