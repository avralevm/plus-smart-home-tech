package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sensors")
@Builder
public class Sensor {
    @Id
    private String id;
    @Column(name = "hub_id")
    private String hubId;
}
