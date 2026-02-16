package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film.isValid()) {
            film.setId(getNextId());
            log.debug("Фильму {} присвоен id", film);
            films.put(film.getId(), film);
            log.info("В память добавлен фильм: {}", film);
            return film;
        }
        throw new ValidationException();
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (!newFilm.isValid()) {
            throw new ValidationException();
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.info("Фильм {} изменен на {}", oldFilm, newFilm);
            oldFilm.setDuration(newFilm.getDuration());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            return oldFilm;
        }
        log.error("Фильм с id = {} не найден", newFilm.getId());
        throw new NotFoundException();
    }

    private int getNextId() {
        int maxCounterInt = films.keySet()
                .stream()
                .mapToInt(i -> i)
                .max()
                .orElse(0);
        return ++maxCounterInt;
    }
}
