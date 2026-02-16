package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.isValid()) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
                log.debug("Пользователю {} в качестве имени присвоен логин", user);
            }
            user.setId(getNextId());
            log.debug("Пользователю {} присвоен id", user);
            users.put(user.getId(), user);
            log.info("В память добавлен пользователь: {}", user);
            return user;
        }
        throw new ValidationException();
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (!newUser.isValid()) {
            throw new ValidationException();
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            log.info("Пользователь {} изменен на {}", oldUser, newUser);
            oldUser.setName(newUser.getName());
            oldUser.setBirthday(newUser.getBirthday());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setEmail(newUser.getEmail());
            return oldUser;
        }
        log.error("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException();
    }

    private int getNextId() {
        int maxCounterInt = users.keySet()
                .stream()
                .mapToInt(i -> i)
                .max()
                .orElse(0);
        return ++maxCounterInt;
    }
}
