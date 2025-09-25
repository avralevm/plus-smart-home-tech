package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;

import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.store.ProductDto;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toProductDto(Product product);

    Product toProduct(ProductDto productDto);
}