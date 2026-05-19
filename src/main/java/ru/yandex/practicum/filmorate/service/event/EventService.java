package ru.yandex.practicum.filmorate.service.event;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EventNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    private final EventDbStorage eventDbStorage;
    private final UserStorage userStorage;

    public EventService(EventDbStorage eventDbStorage, @Qualifier("userDbStorage") UserStorage userStorage) {
        this.eventDbStorage = eventDbStorage;
        this.userStorage = userStorage;
    }

    public List<Event> getFeed(Long userId) {
        Optional.ofNullable(userStorage.getUserById(userId))
                .orElseThrow(() -> new EventNotFoundException("Пользователя с id=" + userId + " не существует"));
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
