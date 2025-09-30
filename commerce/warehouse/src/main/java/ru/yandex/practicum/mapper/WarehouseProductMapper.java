package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.model.Dimension;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.DimensionDto;
import ru.yandex.practicum.warehouse.NewProductInWarehouseRequest;

@Mapper(componentModel = "spring")
public interface WarehouseProductMapper {
    @Mapping(target = "dimension", source = "dimension")
    WarehouseProduct toWarehouseProduct(NewProductInWarehouseRequest request);

    Dimension toDimension(DimensionDto dto);
}