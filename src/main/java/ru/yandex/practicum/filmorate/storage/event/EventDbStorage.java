package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Event> getFeed(Long userId) {
        String sql = "SELECT * " +
                "FROM feed " +
                "WHERE user_id = ? ";

        return jdbcTemplate.query(sql, this::mapEvent, userId);
    }

    public void createEvent(Event event) {
        String sql = "INSERT INTO feed (user_id, timestamp, event_type, operation, entity_id) values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                event.getUserId(),
                event.getTimestamp(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );
    }

    public Event mapEvent(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getInt("event_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(EventOperation.valueOf(rs.getString("operation")))
                .timestamp(rs.getLong("timestamp"))
                .userId(rs.getLong("user_id"))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}
