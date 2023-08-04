package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class Event {
    private Long eventId;

    private Long userId;

    private Long entityId;

    private EventType eventType;

    private EventOperation operation;

    private long timestamp;
}
