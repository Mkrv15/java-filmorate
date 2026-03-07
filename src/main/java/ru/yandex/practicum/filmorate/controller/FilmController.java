package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    private final AtomicInteger atomicInteger = new AtomicInteger();

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        film.setId(getNextId());
        log.debug("Фильму {} присвоен id", film);
        films.put(film.getId(), film);
        log.info("В память добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.replace(newFilm.getId(), newFilm);
            log.info("Фильм {} изменен на {}", oldFilm, newFilm);
            return newFilm;
        }
        log.error("Фильм с id = {} не найден", newFilm.getId());
        throw new NotFoundException();
    }

    private int getNextId() {
        return atomicInteger.incrementAndGet();
    }
}
