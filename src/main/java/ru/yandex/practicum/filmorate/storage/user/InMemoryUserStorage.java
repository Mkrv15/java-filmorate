package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger atomicInteger = new AtomicInteger();

    @Override
    public User create(User user) {
        user.setId(getNextId());
        log.debug("Пользователю {} присвоен id", user);
        users.put(user.getId(), user);
        log.info("В память добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User delete(Long id) {
        User remUser = users.remove(id);
        if (remUser == null) {
            log.error("Пользователь с id = {} не найден", id);
            throw new NotFoundException("Пользователь1 с id = " + id + " не найден");
        }
        log.info("Пользователь {} удален", remUser);
        return remUser;
    }

    @Override
    public User update(User newUser) {
        User oldUser = users.replace(newUser.getId(), newUser);
        if (oldUser == null) {
            log.error("Пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь2 с id = " + newUser.getId() + " не найден");
        }
        log.info("Пользователь {} изменен на {}", oldUser, newUser);
        return newUser;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(long id) {
        User user = users.get(id);
        if (user == null) {
            log.error("Пользователь с id = {} не найден", id);
            throw new NotFoundException("Пользователь3 с id = " + id + " не найден");
        }
        return user;
    }

    private int getNextId() {
        return atomicInteger.incrementAndGet();
    }
}
