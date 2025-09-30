package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.store.ProductCategory;
import ru.yandex.practicum.store.ProductState;
import ru.yandex.practicum.store.QuantityState;
import ru.yandex.practicum.repository.ShoppingStoreRepository;
import ru.yandex.practicum.store.ProductDto;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreServiceImpl implements ShoppingStoreService {
    private final ShoppingStoreRepository repository;
    private final ProductMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        Page<Product> productsPage = repository.findAllByProductCategory(category, pageable);
        return productsPage.map(mapper::toProductDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        return mapper.toProductDto(findProductOrThrow(productId));
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product createdProduct = repository.save(mapper.toProduct(productDto));
        return mapper.toProductDto(createdProduct);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        findProductOrThrow(productDto.getProductId());
        Product updatedProduct = repository.save(mapper.toProduct(productDto));
        log.info("Товар был обновлён: {}", updatedProduct);
        return mapper.toProductDto(updatedProduct);
    }

    @Override
    @Transactional
    public boolean removedProductById(UUID productId) {
        Product product = findProductOrThrow(productId);
        product.setProductState(ProductState.DEACTIVATE);
        repository.save(product);
        log.info("Товар с id {}, был удалён", productId);
        return true;
    }

    @Override
    @Transactional
    public boolean setProductQuantityState(UUID productId, QuantityState state) {
        Product product = findProductOrThrow(productId);
        product.setQuantityState(state);
        repository.save(product);
        return true;
    }

    private Product findProductOrThrow(UUID productId) {
        return repository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Товар с id {} не найден", productId);
                    return new NotFoundException(String.format("Товар с id = %s не найден", productId));
                });
    }
}
