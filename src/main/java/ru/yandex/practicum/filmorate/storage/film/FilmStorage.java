package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.dto.FilmSearchBy;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {
    List<Film> getFilms();

    Film create(Film film);

    Film update(Film film);

    Film getFilmById(Long filmId);

    Film delete(Long filmId);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getFilmsByYear(Long directorId);

    List<Film> getFilmsByLikes(Long directorId);

    List<Film> getRecommendations(Long userId);

    List<Film> getFilmsByQuery(String query, Set<FilmSearchBy> by);
}
