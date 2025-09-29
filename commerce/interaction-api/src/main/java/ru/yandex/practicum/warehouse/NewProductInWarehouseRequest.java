package ru.yandex.practicum.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewProductInWarehouseRequest {
    @NotNull
    private UUID productId;

    private boolean fragile;

    @NotNull
    private DimensionDto dimension;

    @NotNull
    private BigDecimal weight;
}