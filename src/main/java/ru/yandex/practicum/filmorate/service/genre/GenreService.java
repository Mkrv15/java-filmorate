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

    public Set<Genre> getFilmGenres(Long filmId) {
        return new HashSet<>(genreDbStorage.getFilmGenres(filmId));
    }

    public void setGenreNamesAndSave(Film film) {
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                Genre fullGenre = getGenreById(genre.getId());
                genre.setName(fullGenre.getName());
            }
            genreDbStorage.delete(film);
            genreDbStorage.add(film);
        }
    }

    public void updateFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genre> sortGenres = film.getGenres().stream()
                    .sorted(Comparator.comparing(Genre::getId))
                    .collect(Collectors.toList());
            film.setGenres(new LinkedHashSet<>(sortGenres));

            for (Genre genre : film.getGenres()) {
                genre.setName(getGenreById(genre.getId()).getName());
            }
        }
        genreDbStorage.delete(film);
        genreDbStorage.add(film);
    }
}
