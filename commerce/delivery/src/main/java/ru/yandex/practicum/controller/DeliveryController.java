package ru.yandex.practicum.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.client.DeliveryFeignClient;
import ru.yandex.practicum.delivery.DeliveryDto;
import ru.yandex.practicum.order.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryFeignClient {
    private final DeliveryService service;

    @PutMapping
    public DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto deliveryDto) {
        log.info("[PUT] Создание новой доставки: {}", deliveryDto);
        return service.planDelivery(deliveryDto);
    }

    @PostMapping("/successful")
    public void successfulDelivery(@RequestBody @NotNull UUID deliveryId) {
        log.info("[POST] Подтверждение успешной доставки id заказа: {}", deliveryId);
        service.successfulDelivery(deliveryId);
    }

    @PostMapping("/picked")
    public void pickedDelivery(@RequestBody @NotNull UUID deliveryId) {
        log.info("[POST] Подтверждение успешного получение id заказа: {}", deliveryId);
        service.pickedDelivery(deliveryId);
    }

    @PostMapping("/failed")
    public void failedDelivery(@RequestBody @NotNull UUID deliveryId) {
        log.info("[POST] Неудачное вручение товара id заказа: {}", deliveryId);
        service.failedDelivery(deliveryId);
    }

    @PostMapping("/cost")
    public BigDecimal deliveryCost(@RequestBody @Valid OrderDto orderDto) {
        log.info("[POST] Расчёт полной стоимости доставки заказа: {}", orderDto);
        return service.calculateDeliveryCost(orderDto);
    }
}