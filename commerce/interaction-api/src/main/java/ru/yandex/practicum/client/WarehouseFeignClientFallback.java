package ru.yandex.practicum.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.warehouse.*;

import java.util.Map;
import java.util.UUID;

@Component
public class WarehouseFeignClientFallback implements WarehouseProductFeignClient {
    @Override
    public void addProductInWarehouse(NewProductInWarehouseRequest request) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Warehouse temporarily unavailable. Please try again later.");
    }

    @Override
    public void AddProductToWarehouse(AddProductToWarehouseRequest request) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Warehouse temporarily unavailable. Please try again later.");
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Warehouse temporarily unavailable. Please try again later.");
    }

    @Override
    public AddressDto getAddressWarehouse() {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Warehouse temporarily unavailable. Please try again later.");
    }

    @Override
    public void shippedToDelivery(ShipperToDeliveryRequest request) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Warehouse temporarily unavailable. Please try again later.");
    }

    @Override
    public void returnProducts(Map<UUID, Long> products) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Warehouse temporarily unavailable. Please try again later.");
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Warehouse temporarily unavailable. Please try again later.");
    }
}