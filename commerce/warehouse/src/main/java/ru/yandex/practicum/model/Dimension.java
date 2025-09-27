package ru.yandex.practicum.model;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dimension {
    private Double width;
    private Double height;
    private Double depth;
}
