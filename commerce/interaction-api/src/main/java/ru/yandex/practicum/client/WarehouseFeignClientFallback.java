package ru.yandex.practicum.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.AddressDto;
import ru.yandex.practicum.warehouse.BookedProductsDto;
import ru.yandex.practicum.warehouse.NewProductInWarehouseRequest;

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
}