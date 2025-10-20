package ru.yandex.practicum.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.store.ProductCategory;
import ru.yandex.practicum.store.ProductDto;
import ru.yandex.practicum.store.SetProductQuantityStateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreFeignClient {
    @GetMapping
    Page<ProductDto> getProducts(@RequestParam ProductCategory category, Pageable pageable);

    @GetMapping("{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @PutMapping
    ProductDto createProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    boolean removedProductById(@RequestBody @NotNull UUID productId);

    @PostMapping("/quantityState")
    boolean setProductQuantityState(@RequestBody @Valid SetProductQuantityStateRequest request);

    @GetMapping("/by-ids")
    List<ProductDto> getProductsByIds(@RequestBody List<UUID> uuids);
}