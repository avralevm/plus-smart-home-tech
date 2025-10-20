package ru.yandex.practicum.service;

import ru.yandex.practicum.order.OrderDto;
import ru.yandex.practicum.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    PaymentDto createPayment(OrderDto orderDto);

    BigDecimal calculateTotalPayment(OrderDto orderDto);

    BigDecimal calculateProductPayment(OrderDto orderDto);

    void refundPayment(UUID paymentId);

    void failPayment(UUID paymentId);
}
