package ru.yandex.practicum.filmorate.service.director;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
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

    public Map<Long, Set<Director>> getFilmDirectorsBatch(List<Long> filmIds) {
        return directorDbStorage.getFilmDirectorsBatch(filmIds);
    }
}
