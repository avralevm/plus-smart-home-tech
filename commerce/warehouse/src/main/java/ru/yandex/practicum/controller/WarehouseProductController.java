package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.client.WarehouseProductFeignClient;
import ru.yandex.practicum.service.WarehouseProductService;
import ru.yandex.practicum.warehouse.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseProductController implements WarehouseProductFeignClient {
    private final WarehouseProductService service;

    @PutMapping
    public void addProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request) {
        log.info("[PUT] Запрос на добавление товара для обработки складом: {},", request);
        service.addProductInWarehouse(request);
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid @RequestBody ShoppingCartDto shoppingCartDto) {
        log.info("[POST] Проверка количества товаров на складе для данной корзины продуктов: {}", shoppingCartDto);
        return service.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);
    }

    @PostMapping("/add")
    public void AddProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request) {
        log.info("[POST] Запрос на добавление определенного количества определенного товара: {}", request);
        service.addProductToWarehouse(request);
    }

    @GetMapping("/address")
    public AddressDto getAddressWarehouse() {
        log.info("[GET] Получение адреса склада");
        return service.getAddressWarehouse();
    }

    @PostMapping("/shipped")
    public void shippedToDelivery(@Valid @RequestBody ShipperToDeliveryRequest request) {
        log.info("[POST] Запрос на передачу товара в доставку: {}", request);
        service.shippedToDelivery(request);
    }

    @PostMapping("/return")
    public void returnProducts(@RequestBody Map<UUID, Long> products) {
        log.info("[POST] Возврат товаров на склад: {}", products);
        service.returnProducts(products);
    }

    @PostMapping("/assembly")
    public BookedProductsDto assemblyProductsForOrder(@Valid @RequestBody AssemblyProductsForOrderRequest request) {
        log.info("[POST] Запрос на сборку товаров: {}", request);
        return service.assemblyProductsForOrder(request);
    }
}