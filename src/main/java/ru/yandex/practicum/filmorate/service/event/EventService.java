package ru.yandex.practicum.filmorate.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EventNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventDbStorage eventDbStorage;

    public List<Event> getFeed(Long userId) {
        if (userId == null) throw new EventNotFoundException("Пользователя с id=" + userId + " не найден");

        return eventDbStorage.getFeed(userId);
    }

    public void createEvent(Long userId, EventType eventType, EventOperation eventOperation, Long entityId) {
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(eventOperation)
                .entityId(entityId)
                .build();

        eventDbStorage.createEvent(event);
    }
}
