package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private final AtomicInteger id = new AtomicInteger();

    @Override
    public Film create(Film film) {
        if (!film.isValid()) {
            throw new ValidationException();
        }
        film.setId(getNextId());
        log.debug("Фильму {} присвоен id", film);
        films.put(film.getId(), film);
        log.info("В память добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film delete(Long id) {
        Film remFilm = films.remove(id);
        if (remFilm == null) {
            log.error("Фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        log.info("Фильм {} удален", remFilm);
        return remFilm;
    }

    @Override
    public Film update(Film newFilm) {
        Film oldFilm = films.replace(newFilm.getId(), newFilm);
        if (oldFilm == null) {
            log.error("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм1 с id = " + newFilm.getId() + " не найден");
        }
        log.info("Фильм {} изменен на {}", oldFilm, newFilm);
        return newFilm;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.error("Фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return film;
    }

    private int getNextId() {
        return id.incrementAndGet();
    }
}
