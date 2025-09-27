package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.DimensionDto;
import ru.yandex.practicum.warehouse.NewProductInWarehouseRequest;

@Mapper(componentModel = "spring")
public interface WarehouseProductMapper {
    Dimension dtoToDimension(DimensionDto dto);

    DimensionDto dimensionToDto(Dimension dimension);
}