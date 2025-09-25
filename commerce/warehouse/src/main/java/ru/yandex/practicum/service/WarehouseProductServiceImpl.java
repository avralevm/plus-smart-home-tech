package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.cart.ShoppingCartDto;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseProductRepository;
import ru.yandex.practicum.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.AddressDto;
import ru.yandex.practicum.warehouse.BookedProductsDto;
import ru.yandex.practicum.warehouse.NewProductInWarehouseRequest;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseProductServiceImpl implements WarehouseProductService {
    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];
    private final WarehouseProductRepository repository;
    private final WarehouseProductMapper mapper;

    @Override
    @Transactional
    public void addProductInWarehouse(NewProductInWarehouseRequest request) {
        if (repository.findById(request.getProductId()).isPresent()){
            throw new SpecifiedProductAlreadyInWarehouseException
                    ("Товар с id: " + request.getProductId() + " уже зарегистрирован на складе");
        }
        WarehouseProduct product = repository.save(mapper.toWarehouseProduct(request));
        log.info("Создан product: {}", product);
    }

    @Override
    @Transactional
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Long> cartProducts = shoppingCartDto.getProducts();
        Map<UUID, WarehouseProduct> products = repository.findAllById(cartProducts.keySet())
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
        if (products.size() != cartProducts.size()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("Некоторых товаров нет на складе");
        }
        double weight = 0;
        double volume = 0;
        boolean fragile = false;
        for (Map.Entry<UUID, Long> cartProduct : cartProducts.entrySet()) {
            WarehouseProduct product = products.get(cartProduct.getKey());
            if (cartProduct.getValue() > product.getQuantity()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Ошибка, товар из корзины не находится в требуемом количестве на складе: " + cartProduct.getValue());
            }
            Dimension dimension = product.getDimension();
            weight += dimension.getWidth() * cartProduct.getValue();
            volume += dimension.getHeight() * product.getWeight() * dimension.getDepth() * cartProduct.getValue();
            fragile = fragile || product.isFragile();
        }
        return new BookedProductsDto(weight, volume, fragile);
    }

    @Override
    @Transactional
    public void AddProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct product = repository.findById(request.getProductId()).orElseThrow(() -> {
            log.error("Нет информации о товаре на складе c id: {}", request.getProductId());
            return new NoSpecifiedProductInWarehouseException("Нет информации о товаре на складе id: " + request.getProductId());
        });
        product.setQuantity(product.getQuantity() + request.getQuantity());
        WarehouseProduct updatedProduct = repository.save(product);
        log.info("Обновление количества product: {}", updatedProduct);
    }

    @Override
    public AddressDto getAddressWarehouse() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }
}