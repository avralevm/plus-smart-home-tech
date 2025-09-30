package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.cart.ShoppingCartState;
import ru.yandex.practicum.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.client.WarehouseProductFeignClient;
import ru.yandex.practicum.exception.DeactivateShoppingCart;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository repository;
    private final ShoppingCartMapper mapper;
    private final WarehouseProductFeignClient warehouseClient;

    @Override
    @Transactional
    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCart shoppingCart = findOrCreate(username);
        return mapper.toShoppingCartDto(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        ShoppingCart shoppingCart = findOrCreate(username);

        if (shoppingCart.getState() == ShoppingCartState.DEACTIVATE) {
            throw new DeactivateShoppingCart("Нельзя добавлять товары. Корзина деактивирована");
        }

        warehouseClient.checkProductQuantityEnoughForShoppingCart(new ShoppingCartDto(shoppingCart.getShoppingCartId(), products));

        shoppingCart.getProducts().putAll(products);
        ShoppingCart addShoppingCart = repository.save(shoppingCart);
        return mapper.toShoppingCartDto(addShoppingCart);
    }

    @Override
    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        ShoppingCart shoppingCart = findShoppingCart(username);

        if (shoppingCart.getState() == ShoppingCartState.DEACTIVATE) {
            return;
        }

        shoppingCart.setState(ShoppingCartState.DEACTIVATE);
        repository.save(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        ShoppingCart shoppingCart = findShoppingCart(username);

        products.forEach(shoppingCart.getProducts()::remove);
        ShoppingCart removeShoppingCart = repository.save(shoppingCart);

        return mapper.toShoppingCartDto(removeShoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart shoppingCart = findShoppingCart(username);

        if (shoppingCart.getState() == ShoppingCartState.DEACTIVATE) {
            throw new DeactivateShoppingCart("Нельзя добавлять товары. Корзина деактивирована");
        }
        if (!shoppingCart.getProducts().containsKey(request.getProductId())) {
            throw new NoProductsInShoppingCartException("Нет искомых товаров в корзине");
        }
        shoppingCart.getProducts().put(request.getProductId(), request.getNewQuantity());
        ShoppingCart changedShoppingCart = repository.save(shoppingCart);
        return mapper.toShoppingCartDto(changedShoppingCart);
    }

    private ShoppingCart findOrCreate(String username) {
        return repository.findByUsername(username)
                .orElseGet(() -> createNewShoppingCart(username));
    }

    private ShoppingCart findShoppingCart(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Корзина покупателя: " + username + " не найдена"));
    }

    private ShoppingCart createNewShoppingCart(String username) {
        ShoppingCart newShoppingCart = ShoppingCart.builder()
                .shoppingCartId(UUID.randomUUID())
                .username(username)
                .products(new HashMap<>())
                .state(ShoppingCartState.ACTIVE)
                .build();
        return repository.save(newShoppingCart);
    }
}