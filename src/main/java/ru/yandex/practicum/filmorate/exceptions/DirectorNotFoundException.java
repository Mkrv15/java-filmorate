package ru.yandex.practicum.filmorate.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectorNotFoundException extends IllegalArgumentException {
    public DirectorNotFoundException(String message) {
        super(message);
        log.error(message);
    }
}
