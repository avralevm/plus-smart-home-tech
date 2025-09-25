package ru.yandex.practicum.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.client.ShoppingCartFeignClient;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartFeignClient {
    private final ShoppingCartService service;

    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam @NotBlank String username) {
        log.info("[GET] Получение товара username: {}", username);
        return service.getShoppingCart(username);
    }

    @PutMapping
    public ShoppingCartDto addProductToShoppingCart(@RequestParam @NotBlank String username,
                                                    @RequestBody Map<UUID, Long> products) {
        log.info("[PUT] Добавление товара в корзину username: {}, products: {}", username, products);
        return service.addProductToShoppingCart(username, products);
    }

    @DeleteMapping
    public void deactivateCurrentShoppingCart(@RequestParam String username) {
        log.info("[DELETE] Деактивация корзины товаров для username: {}", username);
        service.deactivateCurrentShoppingCart(username);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeFromShoppingCart(@RequestParam String username, @RequestBody List<UUID> products) {
        log.info("[POST] Удаление товаров: {} из корзины username: {}", products, username);
        return service.removeFromShoppingCart(username, products);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam String username, @RequestBody ChangeProductQuantityRequest request) {
        log.info("[POST] Изменить количество товаров в корзине для username: {}, request: {}", username, request);
        return service.changeProductQuantity(username, request);
    }
}
