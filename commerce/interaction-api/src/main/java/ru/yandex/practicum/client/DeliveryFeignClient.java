package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.delivery.DeliveryDto;
import ru.yandex.practicum.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryFeignClient {
    @PutMapping
    DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto deliveryDto);

    @PostMapping("/successful")
    void successfulDelivery(@RequestBody @NotNull UUID deliveryId);

    @PostMapping("/picked")
    void pickedDelivery(@RequestBody @NotNull UUID deliveryId);

    @PostMapping("/failed")
    void failedDelivery(@RequestBody @NotNull UUID deliveryId);

    @PostMapping("/cost")
    BigDecimal deliveryCost(@RequestBody @Valid OrderDto orderDto);
}
