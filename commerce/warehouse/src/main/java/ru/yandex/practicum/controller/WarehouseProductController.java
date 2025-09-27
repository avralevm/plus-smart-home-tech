package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.client.WarehouseProductFeignClient;
import ru.yandex.practicum.service.WarehouseProductService;
import ru.yandex.practicum.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.AddressDto;
import ru.yandex.practicum.warehouse.BookedProductsDto;
import ru.yandex.practicum.warehouse.NewProductInWarehouseRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseProductController implements WarehouseProductFeignClient {
    private final WarehouseProductService service;

    @PutMapping
    public void addProductInWarehouse(@RequestBody NewProductInWarehouseRequest request) {
        log.info("[PUT] Запрос на добавление товара для обработки складом: {},", request);
        service.addProductInWarehouse(request);
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCartDto) {
        log.info("[POST] Проверка количества товаров на складе для данной корзины продуктов: {}", shoppingCartDto);
        return service.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);
    }

    @PostMapping("/add")
    public void AddProductToWarehouse(@RequestBody AddProductToWarehouseRequest request) {
        log.info("[POST] Запрос на добавление определенного количества определенного товара: {}", request);
        service.addProductToWarehouse(request);
    }

    @GetMapping("address")
    public AddressDto getAddressWarehouse() {
        log.info("[GET] Получение адреса склада");
        return service.getAddressWarehouse();
    }
}