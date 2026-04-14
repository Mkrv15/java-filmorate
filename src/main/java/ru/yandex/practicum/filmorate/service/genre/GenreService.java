package ru.yandex.practicum.filmorate.service.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> getGenres() {
        return genreDbStorage.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toList());
    }

    public Genre getGenreById(Integer id) {
        return genreDbStorage.getGenreById(id);
    }

    public void putGenres(Film film) {
        genreDbStorage.delete(film);
        genreDbStorage.add(film);
    }

    public Set<Genre> getFilmGenres(Long filmId) {
        return new HashSet<>(genreDbStorage.getFilmGenres(filmId));
    }
}
