package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;

import java.util.List;

public interface EventStorage {
        Event addEvent(long userId, EventType eventType, EventOperation eventOperation, long entityId);

        Event saveEvent(Event event);

        List<Event> getEventOfUser(long userId);
}
