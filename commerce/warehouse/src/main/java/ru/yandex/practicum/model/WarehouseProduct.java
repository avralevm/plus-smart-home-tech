package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseProduct {
    @Id
    private UUID productId;

    @Column
    private Double weight;

    @Embedded
    private Dimension dimension;

    @Column
    private boolean fragile;

    @Column
    private Long quantity;
}