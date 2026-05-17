package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmSearchBy;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {
    private final EventService eventService;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private LikeDbStorage likeStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikeDbStorage likeDbStorage, EventService eventService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeDbStorage;
        this.eventService = eventService;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film != null) {
            if (userStorage.getUserById(userId) != null) {
                if (!likeStorage.getLikes(filmId).contains(userId)) likeStorage.addLike(filmId, userId);

            } else {
                throw new UserNotFoundException("Пользователь c ID=" + userId + " не найден!");
            }
        } else {
            throw new FilmNotFoundException("Фильм c ID=" + filmId + " не найден!");
        }
        eventService.createEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film != null) {
            if (film.getLikes().contains(userId)) {
                likeStorage.deleteLike(filmId, userId);
            } else {
                throw new UserNotFoundException("Лайк от пользователя c ID=" + userId + " не найден!");
            }
        } else {
            throw new FilmNotFoundException("Фильм c ID=" + filmId + " не найден!");
        }
        eventService.createEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
    }

    public List<Film> getPopular(Integer count, Integer genreId, Integer year) {
        if (count < 1) {
            throw new ValidationException("Количество фильмов для вывода не должно быть меньше 1");
        }

        if (genreId == null && year == null) {
            return likeStorage.getPopular(count);
        }
        return likeStorage.getPopularByGenreAndYear(count, genreId, year);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getDirectorFilms(Long directorId, String sortBy) {
        List<Film> films = new ArrayList<>();
        switch (sortBy) {
            case "year":
                films = filmStorage.getFilmsByYear(directorId);
                break;
            case "likes":
                films = filmStorage.getFilmsByLikes(directorId);
                break;
        }
        if (films.isEmpty()) {
            throw new FilmNotFoundException("Не найдены фильмы указанного режиссера");
        }
        return films;
    }

    public List<Film> getRecommendations(Long userId) {
        return filmStorage.getRecommendations(userId);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film delete(Long filmId) {
        return filmStorage.delete(filmId);
    }

    public List<Film> search(String query, Set<FilmSearchBy> by) {
        Set<FilmSearchBy> effectiveBy = (by == null || by.isEmpty())
                ? EnumSet.of(FilmSearchBy.TITLE)
                : by;
        return filmStorage.getFilmsByQuery(query, effectiveBy);
    }
}
