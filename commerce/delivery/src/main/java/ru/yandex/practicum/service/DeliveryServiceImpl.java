package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.OrderFeignClient;
import ru.yandex.practicum.client.WarehouseProductFeignClient;
import ru.yandex.practicum.delivery.DeliveryDto;
import ru.yandex.practicum.delivery.DeliveryState;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.order.OrderDto;
import ru.yandex.practicum.repository.DeliveryRepository;
import ru.yandex.practicum.warehouse.ShipperToDeliveryRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository repository;
    private final DeliveryMapper mapper;
    private final OrderFeignClient orderClient;
    private final WarehouseProductFeignClient warehouseClient;

    private static final double BASE_COST = 5.0;
    private static final double WAREHOUSE_2_ADDRESS_MULTIPLIER = 2;
    private static final double FRAGILE_MULTIPLIER = 0.2;
    private static final double WEIGHT_MULTIPLIER = 0.3;
    private static final double VOLUME_MULTIPLIER = 0.2;
    private static final double STREET_MULTIPLIER = 0.2;

    @Override
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = mapper.toDelivery(deliveryDto);
        Delivery savedDelivery = repository.save(delivery);
        return mapper.toDeliveryDto(savedDelivery);
    }

    @Override
    @Transactional
    public void successfulDelivery(UUID deliveryId) {
        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setState(DeliveryState.DELIVERED);
        repository.save(delivery);
        orderClient.deliveryOrder(delivery.getOrderId());
    }

    @Override
    @Transactional
    public void pickedDelivery(UUID deliveryId) {
        Delivery delivery = findDeliveryOrThrow(deliveryId);
        UUID orderId = delivery.getOrderId();
        delivery.setState(DeliveryState.IN_PROGRESS);
        orderClient.assemblyOrder(orderId);
        warehouseClient.shippedToDelivery(new ShipperToDeliveryRequest(orderId, deliveryId));
        repository.save(delivery);
    }

    @Override
    @Transactional
    public void failedDelivery(UUID deliveryId) {
        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setState(DeliveryState.FAILED);
        orderClient.failDeliveryOrder(delivery.getOrderId());
        repository.save(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDeliveryCost(OrderDto orderDto) {
        Delivery delivery = findDeliveryOrThrow(orderDto.getDeliveryId());
        Address from = delivery.getFromAddress();
        Address to = delivery.getToAddress();

        double cost = BASE_COST;

        if (from.getStreet().contains("ADDRESS_2")) {
            cost += cost * WAREHOUSE_2_ADDRESS_MULTIPLIER;
        }

        if (orderDto.isFragile()) {
            cost += cost * FRAGILE_MULTIPLIER;
        }

        cost += orderDto.getDeliveryWeight().doubleValue() * WEIGHT_MULTIPLIER;
        cost += orderDto.getDeliveryVolume().doubleValue() * VOLUME_MULTIPLIER;

        if (!from.getStreet().equalsIgnoreCase(to.getStreet())) {
            cost += cost * STREET_MULTIPLIER;
        }

        return BigDecimal.valueOf(cost).setScale(2, RoundingMode.HALF_UP);
    }

    private Delivery findDeliveryOrThrow(UUID deliveryId) {
        return repository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new NotFoundException("Доставка с id: " + deliveryId + " не найдена"));
    }
}