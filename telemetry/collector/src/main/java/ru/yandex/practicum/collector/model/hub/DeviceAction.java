package ru.yandex.practicum.collector.model.hub;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class DeviceAction {
    private String sensorId;

    private ActionType type;

    private int value;
}
