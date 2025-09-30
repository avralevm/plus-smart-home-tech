package ru.yandex.practicum.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DimensionDto {
    @NotNull
    private BigDecimal width;

    @NotNull
    private BigDecimal height;

    @NotNull
    private BigDecimal depth;
}
