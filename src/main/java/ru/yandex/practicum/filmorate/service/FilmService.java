package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public void addLike(long userId, long filmId) {
        Film film = filmStorage.findById(filmId);
        User user = userStorage.findById(userId);
        if (film.getLikes().contains(userId)) {
            log.info("У пользователя {} уже стоит лайк на фильм {}", user, film);
        }
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, film);
    }

    public void removeLike(long userId, long filmId) {
        Film film = filmStorage.findById(filmId);
        User user = userStorage.findById(userId);
        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            log.info("Пользователь {} убрал лайк с фильма {}", user, film);
            return;
        }
        log.error("Пользователь с id {} ставивший лайк фильму {} не найден", userId, film);
        throw new NotFoundException("Пользователь с id " + userId + " ставивший лайк фильму " + film + " не найден");
    }

    public List<Film> getTopPopularFilms(long count) {
        return filmStorage.findAll()
                .stream()
                .sorted(Comparator.comparingInt((Film film) ->
                        film.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
