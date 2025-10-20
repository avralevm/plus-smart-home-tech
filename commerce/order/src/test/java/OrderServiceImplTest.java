import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.client.DeliveryFeignClient;
import ru.yandex.practicum.client.PaymentFeignClient;
import ru.yandex.practicum.client.WarehouseProductFeignClient;
import ru.yandex.practicum.delivery.DeliveryDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.order.CreateNewOrderRequest;
import ru.yandex.practicum.order.OrderDto;
import ru.yandex.practicum.order.OrderState;
import ru.yandex.practicum.order.ProductReturnRequest;
import ru.yandex.practicum.payment.PaymentDto;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.service.OrderServiceImpl;
import ru.yandex.practicum.warehouse.AddressDto;
import ru.yandex.practicum.warehouse.BookedProductsDto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository repository;
    @Mock
    private OrderMapper mapper;
    @Mock
    private WarehouseProductFeignClient warehouseClient;
    @Mock
    private PaymentFeignClient paymentClient;
    @Mock
    private DeliveryFeignClient deliveryClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID orderId;
    private Order order;
    private OrderDto orderDto;
    private ShoppingCartDto cart;
    private AddressDto address;

    @BeforeEach
    void setup() {
        orderId = UUID.randomUUID();
        order = Order.builder().orderId(orderId).state(OrderState.NEW).build();
        orderDto = OrderDto.builder()
                .orderId(orderId)
                .state(OrderState.NEW)
                .products(Map.of())
                .build();
        cart = new ShoppingCartDto();
        cart.setProducts(Map.of(UUID.randomUUID(), 1L));
        address = new AddressDto();
    }

    @Test
    void createOrder_shouldReturnOrderDto() {
        when(warehouseClient.checkProductQuantityEnoughForShoppingCart(cart))
                .thenReturn(new BookedProductsDto(BigDecimal.ONE, BigDecimal.ONE, false));
        when(repository.save(any(Order.class))).thenReturn(order);
        when(deliveryClient.planDelivery(any())).thenReturn(
                DeliveryDto.builder().deliveryId(UUID.randomUUID()).build());
        when(paymentClient.createPayment(any())).thenReturn(
                PaymentDto.builder().totalPayment(BigDecimal.TEN).build());
        when(mapper.toOrderDto(any())).thenReturn(orderDto);

        CreateNewOrderRequest request = new CreateNewOrderRequest(cart, address);
        OrderDto result = orderService.createOrder(request);

        assertNotNull(result);
        verify(repository, atLeastOnce()).save(any());
    }

    @Test
    void returnOrder_shouldCallWarehouseAndChangeState() {
        when(repository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);
        when(mapper.toOrderDto(order)).thenReturn(orderDto);

        ProductReturnRequest request = new ProductReturnRequest(orderId, Map.of(UUID.randomUUID(), 1L));
        OrderDto result = orderService.returnOrder(request);

        assertEquals(OrderState.PRODUCT_RETURNED, order.getState());
        verify(warehouseClient).returnProducts(request.getProducts());
    }

    @Test
    void changeState_shouldThrowIfOrderNotFound() {
        when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.deliveryOrder(orderId));
    }

    @Test
    void calculateTotalOrder_shouldSetTotalPrice() {
        when(repository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);
        when(mapper.toOrderDto(order)).thenReturn(orderDto);
        when(paymentClient.calculateTotalPayment(orderDto)).thenReturn(BigDecimal.TEN);

        OrderDto result = orderService.calculateTotalOrder(orderId);

        assertEquals(BigDecimal.TEN, order.getTotalPrice());
    }

    @Test
    void calculateTotalDeliveryOrder_shouldUpdateDeliveryPriceAndReturnDto() {
        BigDecimal expectedDeliveryCost = BigDecimal.valueOf(12.50);

        when(repository.findByOrderId(orderId)).thenReturn(Optional.of(order));
        when(mapper.toOrderDto(any(Order.class))).thenReturn(orderDto);
        when(deliveryClient.deliveryCost(any(OrderDto.class))).thenReturn(expectedDeliveryCost);
        when(repository.save(order)).thenReturn(order);
        when(mapper.toOrderDto(order)).thenReturn(
                OrderDto.builder()
                        .orderId(orderId)
                        .deliveryPrice(expectedDeliveryCost)
                        .build()
        );

        OrderDto result = orderService.calculateTotalDeliveryOrder(orderId);

        assertNotNull(result);
        assertEquals(expectedDeliveryCost, result.getDeliveryPrice());
        assertEquals(expectedDeliveryCost, order.getDeliveryPrice());

        verify(repository).findByOrderId(orderId);
        verify(deliveryClient).deliveryCost(any(OrderDto.class));
        verify(repository).save(order);
        verify(mapper, atLeastOnce()).toOrderDto(order);
    }
}