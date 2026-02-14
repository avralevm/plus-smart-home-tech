package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.client.DeliveryFeignClient;
import ru.yandex.practicum.client.PaymentFeignClient;
import ru.yandex.practicum.client.WarehouseProductFeignClient;
import ru.yandex.practicum.delivery.DeliveryDto;
import ru.yandex.practicum.delivery.DeliveryState;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.order.CreateNewOrderRequest;
import ru.yandex.practicum.order.OrderDto;
import ru.yandex.practicum.order.OrderState;
import ru.yandex.practicum.order.ProductReturnRequest;
import ru.yandex.practicum.payment.PaymentDto;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.warehouse.AddressDto;
import ru.yandex.practicum.warehouse.BookedProductsDto;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final WarehouseProductFeignClient warehouseClient;
    private final PaymentFeignClient paymentClient;
    private final DeliveryFeignClient deliveryClient;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrders(String username, Pageable pageable) {
        Page<Order> orders = repository.findByUsername(username, pageable);
        return orders.map(mapper::toOrderDto);
    }

    @Override
    @Transactional
    public OrderDto createOrder(CreateNewOrderRequest request) {
        BookedProductsDto bookedProductsDto = warehouseClient.checkProductQuantityEnoughForShoppingCart(request.getShoppingCart());

        Order order = createNewOrder(request, bookedProductsDto);
        repository.save(order);

        DeliveryDto createdDelivery = planDelivery(order, request.getDeliveryAddress());
        order.setDeliveryId(createdDelivery.getDeliveryId());

        Order orderSetPayment = processPayment(order);

        Order savedOrder = repository.save(orderSetPayment);
        return mapper.toOrderDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto returnOrder(ProductReturnRequest returnRequest) {
        warehouseClient.returnProducts(returnRequest.getProducts());
        return changeState(returnRequest.getOrderId(), OrderState.PRODUCT_RETURNED);
    }

    @Override
    @Transactional
    public OrderDto paymentOrder(UUID orderId) {
        return changeState(orderId, OrderState.PAID);
    }

    @Override
    @Transactional
    public OrderDto failPaymentOrder(UUID orderId) {
        return changeState(orderId, OrderState.PAYMENT_FAILED);
    }

    @Override
    @Transactional
    public OrderDto deliveryOrder(UUID orderId) {
        return changeState(orderId, OrderState.DELIVERED);
    }

    @Override
    @Transactional
    public OrderDto failDeliveryOrder(UUID orderId) {
        return changeState(orderId, OrderState.DELIVERY_FAILED);
    }

    @Override
    @Transactional
    public OrderDto completedOrder(UUID orderId) {
        return changeState(orderId, OrderState.COMPLETED);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalOrder(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        BigDecimal totalPrice = paymentClient.calculateTotalPayment(mapper.toOrderDto(order));
        order.setTotalPrice(totalPrice);
        Order savedOrder = repository.save(order);
        return mapper.toOrderDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalDeliveryOrder(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        BigDecimal deliveryPrice = deliveryClient.deliveryCost(mapper.toOrderDto(order));
        order.setDeliveryPrice(deliveryPrice);
        Order savedOrder = repository.save(order);
        return mapper.toOrderDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto assemblyOrder(UUID orderId) {
        return changeState(orderId, OrderState.ASSEMBLED);
    }

    @Override
    @Transactional
    public OrderDto failAssemblyOrder(UUID orderId) {
        return changeState(orderId, OrderState.ASSEMBLY_FAILED);
    }

    private Order createNewOrder(CreateNewOrderRequest request, BookedProductsDto bookedProductsDto) {
        return Order.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(request.getShoppingCart().getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(bookedProductsDto.getDeliveryWeight())
                .deliveryVolume(bookedProductsDto.getDeliveryVolume())
                .fragile(bookedProductsDto.isFragile())
                .build();
    }

    private DeliveryDto planDelivery(Order order, AddressDto deliveryAddress) {
        AddressDto warehouseAddress = warehouseClient.getAddressWarehouse();

        DeliveryDto delivery = DeliveryDto.builder()
                .orderId(order.getOrderId())
                .fromAddress(warehouseAddress)
                .toAddress(deliveryAddress)
                .state(DeliveryState.CREATED)
                .build();

        return deliveryClient.planDelivery(delivery);
    }

    private Order processPayment(Order order) {
        OrderDto orderDto = mapper.toOrderDto(order);
        PaymentDto createdPayment = paymentClient.createPayment(orderDto);
        order.setPaymentId(createdPayment.getPaymentId());
        order.setTotalPrice(createdPayment.getTotalPayment());
        order.setDeliveryPrice(createdPayment.getDeliveryTotal());
        BigDecimal productPrice = paymentClient.calculateProductPayment(orderDto);
        order.setProductPrice(productPrice);
        return order;
    }

    private OrderDto changeState(UUID orderId, OrderState newState) {
        Order order = findOrderOrThrow(orderId);
        order.setState(newState);
        Order savedOrder = repository.save(order);
        return mapper.toOrderDto(savedOrder);
    }

    private Order findOrderOrThrow(UUID orderId) {
        return repository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ id: " + orderId + " не найден"));
    }
}