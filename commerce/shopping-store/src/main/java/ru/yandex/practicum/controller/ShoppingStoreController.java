package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.client.ShoppingStoreFeignClient;
import ru.yandex.practicum.store.ProductCategory;
import ru.yandex.practicum.service.ShoppingStoreService;
import ru.yandex.practicum.store.ProductDto;
import ru.yandex.practicum.store.SetProductQuantityStateRequest;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreFeignClient {
    private final ShoppingStoreService service;

    @GetMapping
    public Page<ProductDto> getProducts(@RequestParam ProductCategory category, Pageable pageable) {
        log.info("[GET] Получение списка товаров с категорий: {}, пагинацией: {}", category, pageable);
        return service.getProducts(category, pageable);
    }

    @GetMapping("{productId}")
    public ProductDto getProductById(@PathVariable UUID productId) {
        log.info("[GET] Получение товара c id: {}", productId);
        return service.getProductById(productId);
    }

    @PutMapping
    public ProductDto createProduct(@RequestBody ProductDto productDto) {
        log.info("[PUT] Создание нового товара: {}", productDto);
        return service.createProduct(productDto);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        log.info("[POST] Обновление товара в ассортименте: {}", productDto);
        return service.updateProduct(productDto);
    }

    @PostMapping("/removeProductFromStore")
    public boolean removedProductById(@RequestBody UUID productId) {
        log.info("[POST] Удаление товара из ассортимента c id: {}", productId);
        return service.removedProductById(productId);
    }

    @PostMapping("/quantityState")
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        log.info("[POST] Установка статуса по товару c id: {}, статус: {}", request.getProductId(), request.getQuantityState());
        return service.setProductQuantityState(request.getProductId(), request.getQuantityState());
    }
}
