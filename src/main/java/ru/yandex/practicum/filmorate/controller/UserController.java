package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger atomicInteger = new AtomicInteger();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @GetMapping
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User create(@RequestBody User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            violations.forEach(violation -> {
                log.error(violation.getMessage());
            });
            throw new ValidationException();
        }
        user.setId(getNextId());
        log.debug("Пользователю {} присвоен id", user);
        users.put(user.getId(), user);
        log.info("В память добавлен пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        Set<ConstraintViolation<User>> violations = validator.validate(newUser);
        if (!violations.isEmpty()) {
            violations.forEach(violation -> {
                log.error(violation.getMessage());
            });
            throw new ValidationException();
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.replace(newUser.getId(), newUser);
            log.info("Пользователь {} изменен на {}", oldUser, newUser);
            return newUser;
        }
        log.error("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException();
    }

    private int getNextId() {
        return atomicInteger.incrementAndGet();
    }
}
