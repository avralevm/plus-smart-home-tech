package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregatorServiceImpl implements AggregatorService {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    @Override
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshotAvro = snapshots.getOrDefault(
                event.getHubId(),
                createNewSensorsSnapshot(event.getHubId())
        );

        SensorStateAvro oldState = snapshotAvro.getSensorsState().get(event.getId());
        if (oldState != null && oldState.getTimestamp().isAfter(event.getTimestamp()) &&
                oldState.getData().equals(event.getPayload())) {
            return Optional.empty();
        }

        SensorStateAvro newSnapshot = createNewSensorsSnapshot(event);
        snapshotAvro.getSensorsState().put(event.getId(), newSnapshot);
        snapshotAvro.setTimestamp(event.getTimestamp());
        snapshots.put(event.getHubId(), snapshotAvro);
        return Optional.of(snapshotAvro);
    }

    private SensorsSnapshotAvro createNewSensorsSnapshot(String hubId) {
        return SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(Instant.now())
                .setSensorsState(new HashMap<>())
                .build();
    }

    private SensorStateAvro createNewSensorsSnapshot(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
    }
}