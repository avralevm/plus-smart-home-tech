import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.client.*;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.order.OrderDto;
import ru.yandex.practicum.payment.PaymentDto;
import ru.yandex.practicum.payment.PaymentState;
import ru.yandex.practicum.repository.PaymentRepository;
import ru.yandex.practicum.service.PaymentServiceImpl;
import ru.yandex.practicum.store.ProductDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository repository;
    @Mock
    private PaymentMapper mapper;
    @Mock
    private ShoppingStoreFeignClient shoppingFeignClient;
    @Mock
    private OrderFeignClient orderFeignClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID orderId;
    private OrderDto orderDto;
    private Payment payment;
    private PaymentDto paymentDto;

    @BeforeEach
    void setup() {
        orderId = UUID.randomUUID();
        orderDto = OrderDto.builder()
                .orderId(orderId)
                .products(Map.of(UUID.randomUUID(), 2L))
                .deliveryPrice(BigDecimal.TEN)
                .build();

        payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .orderId(orderId)
                .totalPayment(BigDecimal.valueOf(350.00))
                .deliveryTotal(BigDecimal.TEN)
                .feeTotal(BigDecimal.valueOf(30.00))
                .state(PaymentState.PENDING)
                .build();

        paymentDto = PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .totalPayment(payment.getTotalPayment())
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(payment.getFeeTotal())
                .build();
    }

    @Test
    void calculateProductPayment_shouldReturnTotal() {
        ProductDto product = new ProductDto();
        product.setProductId(orderDto.getProducts().keySet().iterator().next());
        product.setPrice(BigDecimal.TEN);
        when(shoppingFeignClient.getProductsByIds(anyList())).thenReturn(List.of(product));

        BigDecimal total = paymentService.calculateProductPayment(orderDto);

        assertEquals(BigDecimal.valueOf(20), total);
    }



    @Test
    void createPayment_shouldSavePayment() {
        when(shoppingFeignClient.getProductsByIds(anyList())).thenReturn(List.of());
        when(repository.save(any(Payment.class))).thenReturn(payment);
        when(mapper.toPaymentDto(payment)).thenReturn(paymentDto);

        PaymentDto result = paymentService.createPayment(orderDto);

        assertNotNull(result);
        assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
        assertEquals(paymentDto.getTotalPayment(), result.getTotalPayment());
        verify(repository).save(any(Payment.class));
        verify(mapper).toPaymentDto(payment);
    }

    @Test
    void refundPayment_shouldSetSuccessAndCallOrderClient() {
        Payment payment = Payment.builder().paymentId(UUID.randomUUID()).orderId(orderId).build();
        when(repository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(repository.save(payment)).thenReturn(payment);

        paymentService.refundPayment(payment.getPaymentId());

        assertEquals(PaymentState.SUCCESS, payment.getState());
        verify(orderFeignClient).paymentOrder(orderId);
    }

    @Test
    void failPayment_shouldSetFailed() {
        Payment payment = Payment.builder().paymentId(UUID.randomUUID()).orderId(orderId).build();
        when(repository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(repository.save(payment)).thenReturn(payment);

        paymentService.failPayment(payment.getPaymentId());

        assertEquals(PaymentState.FAILED, payment.getState());
    }

    @Test
    void findPaymentOrThrow_shouldThrowIfNotFound() {
        UUID paymentId = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> paymentService.failPayment(paymentId));
    }
}
