package ru.yandex.practicum.filmorate.storage.event.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventEnums.EventOperation;
import ru.yandex.practicum.filmorate.model.EventEnums.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Repository
public class DBEventStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBEventStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder().eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .entityId(rs.getLong("entity_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(EventOperation.valueOf(rs.getString("event_operation")))
                .timestamp(rs.getLong("timestamp"))
                .build();
    }

    @Override
    public Event addEvent(long userId, EventType eventType, EventOperation eventOperation, long entityId) {

        Date date = new Date();

        Event event = Event.builder().userId(userId)
                .eventType(eventType)
                .operation(eventOperation)
                .entityId(entityId)
                .timestamp(date.getTime())
                .build();

        saveEvent(event);

        return event;
    }

    @Override
    public Event saveEvent(Event event) {

        String sql = "insert into EVENTS (entity_id, event_type, event_operation, timestamp, user_id)" +
                " values (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"event_id"});
            stmt.setLong(1, event.getEntityId());
            stmt.setString(2, event.getEventType().toString());
            stmt.setString(3, event.getOperation().toString());
            stmt.setLong(4, event.getTimestamp());
            stmt.setLong(5, event.getUserId());
            return stmt;
        }, keyHolder);
        event.setEventId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return event;
    }

    @Override
    public List<Event> getEventOfUser(long userId) {
        String sql = "SELECT * FROM EVENTS WHERE USER_ID = ?";
        return jdbcTemplate.query(sql, this::mapRowToEvent, userId);
    }
}
