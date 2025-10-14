import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseProductRepository;
import ru.yandex.practicum.service.WarehouseProductServiceImpl;
import ru.yandex.practicum.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.warehouse.BookedProductsDto;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseProductServiceImplTest {

    @Mock
    private WarehouseProductRepository warehouseRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private WarehouseProductMapper mapper;

    @InjectMocks
    private WarehouseProductServiceImpl service;

    private UUID productId;
    private WarehouseProduct product;

    @BeforeEach
    void setup() {
        productId = UUID.randomUUID();
        product = WarehouseProduct.builder()
                .productId(productId)
                .quantity(5L)
                .weight(BigDecimal.ONE)
                .dimension(new Dimension(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE))
                .fragile(false)
                .build();
    }


    @Test
    void checkProductQuantityEnough_shouldThrowIfNotEnough() {
        ShoppingCartDto cart = new ShoppingCartDto();
        cart.setProducts(Map.of(productId, 10L));
        when(warehouseRepository.findAllById(anySet())).thenReturn(List.of(product));

        assertThrows(ProductInShoppingCartLowQuantityInWarehouse.class,
                () -> service.checkProductQuantityEnoughForShoppingCart(cart));
    }

    @Test
    void addProductToWarehouse_shouldIncreaseQuantity() {
        AddProductToWarehouseRequest request = new AddProductToWarehouseRequest();
        request.setProductId(productId);
        request.setQuantity(5L);
        when(warehouseRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseRepository.save(product)).thenReturn(product);

        service.addProductToWarehouse(request);

        assertEquals(10L, product.getQuantity());
    }

    @Test
    void returnProducts_shouldIncreaseQuantity() {
        Map<UUID, Long> returned = Map.of(productId, 3L);
        when(warehouseRepository.findAllById(returned.keySet())).thenReturn(List.of(product));

        service.returnProducts(returned);

        assertEquals(8L, product.getQuantity());
    }

    @Test
    void assemblyProductsForOrder_shouldReduceQuantityAndReturnBooked() {
        AssemblyProductsForOrderRequest request = new AssemblyProductsForOrderRequest();
        request.setOrderId(UUID.randomUUID());
        request.setProducts(Map.of(productId, 2L));
        when(warehouseRepository.findAllById(request.getProducts().keySet())).thenReturn(List.of(product));

        BookedProductsDto booked = service.assemblyProductsForOrder(request);

        assertEquals(BigDecimal.valueOf(2), booked.getDeliveryWeight());
        assertEquals(BigDecimal.valueOf(2), booked.getDeliveryVolume());
        assertFalse(booked.isFragile());
        assertEquals(3L, product.getQuantity());
    }

}