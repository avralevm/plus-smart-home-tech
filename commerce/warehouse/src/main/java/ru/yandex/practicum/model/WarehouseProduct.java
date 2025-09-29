package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "warehouse_product")
public class WarehouseProduct {
    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(precision = 19, scale = 4)
    private BigDecimal weight;

    @Embedded
    private Dimension dimension;

    @Column
    private boolean fragile;

    @Column
    private Long quantity = 0L;
}