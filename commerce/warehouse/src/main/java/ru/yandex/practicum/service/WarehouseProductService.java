package ru.yandex.practicum.service;

import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseProductService {
    void addProductInWarehouse(NewProductInWarehouseRequest request);

    BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto);

    void addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getAddressWarehouse();

    void shippedToDelivery(ShipperToDeliveryRequest request);

    void returnProducts(Map<UUID, Long> products);

    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request);
}