import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.yandex.practicum.service.DeliveryServiceImpl;
import ru.yandex.practicum.warehouse.AddressDto;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock
    private DeliveryRepository repository;
    @Mock
    private DeliveryMapper mapper;
    @Mock
    private OrderFeignClient orderClient;
    @Mock
    private WarehouseProductFeignClient warehouseClient;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private UUID deliveryId;
    private UUID orderId;
    private Delivery delivery;
    private DeliveryDto deliveryDto;
    private Address from;
    private Address to;

    @BeforeEach
    void setup() {
        deliveryId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        from = new Address(); from.setStreet("ADDRESS_2 Street");
        to = new Address(); to.setStreet("Some Street");
        delivery = Delivery.builder().deliveryId(deliveryId).orderId(orderId)
                .fromAddress(from).toAddress(to).state(DeliveryState.CREATED).build();
        deliveryDto = DeliveryDto.builder().deliveryId(deliveryId).orderId(orderId)
                .fromAddress(new AddressDto()).toAddress(new AddressDto())
                .state(DeliveryState.CREATED).build();
    }

    @Test
    void planDelivery_shouldSaveAndReturnDto() {
        when(mapper.toDelivery(deliveryDto)).thenReturn(delivery);
        when(repository.save(delivery)).thenReturn(delivery);
        when(mapper.toDeliveryDto(delivery)).thenReturn(deliveryDto);

        DeliveryDto result = deliveryService.planDelivery(deliveryDto);

        assertEquals(deliveryDto, result);
        verify(repository).save(delivery);
    }

    @Test
    void successfulDelivery_shouldSetDeliveredAndCallOrderClient() {
        when(repository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(delivery));
        when(repository.save(delivery)).thenReturn(delivery);

        deliveryService.successfulDelivery(deliveryId);

        assertEquals(DeliveryState.DELIVERED, delivery.getState());
        verify(orderClient).deliveryOrder(orderId);
    }

    @Test
    void pickedDelivery_shouldSetInProgressAndCallClients() {
        when(repository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(delivery));
        when(repository.save(delivery)).thenReturn(delivery);

        deliveryService.pickedDelivery(deliveryId);

        assertEquals(DeliveryState.IN_PROGRESS, delivery.getState());
        verify(orderClient).assemblyOrder(orderId);
        verify(warehouseClient).shippedToDelivery(any());
    }

    @Test
    void failedDelivery_shouldSetFailedAndCallOrderClient() {
        when(repository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(delivery));
        when(repository.save(delivery)).thenReturn(delivery);

        deliveryService.failedDelivery(deliveryId);

        assertEquals(DeliveryState.FAILED, delivery.getState());
        verify(orderClient).failDeliveryOrder(orderId);
    }

    @Test
    void calculateDeliveryCost_shouldCalculateCorrectly() {
        when(repository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(delivery));
        OrderDto orderDto = OrderDto.builder()
                .deliveryId(deliveryId)
                .deliveryWeight(BigDecimal.valueOf(10))
                .deliveryVolume(BigDecimal.valueOf(5))
                .fragile(true)
                .build();

        BigDecimal cost = deliveryService.calculateDeliveryCost(orderDto);

        assertTrue(cost.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void findDeliveryOrThrow_shouldThrowIfNotFound() {
        when(repository.findByDeliveryId(deliveryId)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> deliveryService.failedDelivery(deliveryId));
    }
}