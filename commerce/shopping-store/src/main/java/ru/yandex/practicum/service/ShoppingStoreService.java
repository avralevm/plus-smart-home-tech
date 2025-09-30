package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.store.ProductCategory;
import ru.yandex.practicum.store.QuantityState;
import ru.yandex.practicum.store.ProductDto;

import java.util.UUID;

public interface ShoppingStoreService {
    Page<ProductDto> getProducts(ProductCategory category, Pageable pageable);

    ProductDto getProductById(UUID productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    boolean removedProductById(UUID productId);

    boolean setProductQuantityState(UUID productId, QuantityState state);
}
