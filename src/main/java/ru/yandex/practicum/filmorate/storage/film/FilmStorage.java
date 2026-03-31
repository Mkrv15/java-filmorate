package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film delete(Long id);

    Film update(Film newFilm);

    List<Film> findAll();

    Film findById(Long id);
}
