package ru.yandex.practicum.filmorate.service.director;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    public List<Director> getDirectors() {
        return directorDbStorage.getDirectors().stream()
                .sorted(Comparator.comparing(Director::getId))
                .collect(Collectors.toList());
    }

    public Director getDirectorById(Long id) {
        return directorDbStorage.getDirectorById(id);
    }

    public Director create(Director director) {
        return directorDbStorage.create(director);
    }

    public Director update(Director director) {
        return directorDbStorage.update(director);
    }

    public Director delete(Long id) {
        return directorDbStorage.delete(id);
    }

    public Set<Director> getFilmDirectors(Long filmId) {
        return new HashSet<>(directorDbStorage.getFilmDirectors(filmId));
    }

    public void setDirectorNamesAndSave(Film film) {
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                Director fullDirector = getDirectorById(director.getId());
                director.setName(fullDirector.getName());
            }
            directorDbStorage.delete(film);
            directorDbStorage.add(film);
        }
    }

    public void updateFilmDirectors(Film film) {
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            List<Director> sortDirectors = film.getDirectors().stream()
                    .sorted(Comparator.comparing(Director::getId))
                    .collect(Collectors.toList());
            film.setDirectors(new LinkedHashSet<>(sortDirectors));

            for (Director director : film.getDirectors()) {
                director.setName(getDirectorById(director.getId()).getName());
            }
        }
        directorDbStorage.delete(film);
        directorDbStorage.add(film);
    }
}
