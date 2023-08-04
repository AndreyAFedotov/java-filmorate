package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.EventEnums.EventOperation;
import ru.yandex.practicum.filmorate.model.EventEnums.EventType;

@Data
@Builder
public class Event {
    private Long eventId;

    private Long userId;

    private Long entityId;

    private EventType eventType;

    private EventOperation operation;

    private long timestamp;
}
