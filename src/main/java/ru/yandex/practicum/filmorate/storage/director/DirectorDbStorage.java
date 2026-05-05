package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Director> getDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Director(
                rs.getLong("id"),
                rs.getString("name"))
        );
    }

    public Director create(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");
        director.setId(simpleJdbcInsert.executeAndReturnKey(director.toMap()).longValue());
        log.info("Добавлен новый режиссер с ID={}", director.getId());
        return director;
    }

    public Director update(Director director) {
        if (director.getId() == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        if (getDirectorById(director.getId()) != null) {
            String sql = "UPDATE directors SET name = ? WHERE id = ?;";
            jdbcTemplate.update(sql,
                    director.getName(),
                    director.getId());
            log.info("Режиссер с ID={} успешно обновлен", director.getId());
            return getDirectorById(director.getId());
        } else {
            throw new DirectorNotFoundException("Режиссер с ID=" + director.getId() + " не найден!");
        }
    }

    public Director getDirectorById(Long directorId) {
        if (directorId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Director director;
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM directors WHERE id = ?", directorId);
        if (genreRows.first()) {
            director = new Director(
                    genreRows.getLong("id"),
                    genreRows.getString("name")
            );
        } else {
            throw new DirectorNotFoundException("Режиссер с ID=" + directorId + " не найден!");
        }
        return director;
    }

    public Director delete(Long id) {
        if (id == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Director director = getDirectorById(id);
        if (director != null) {
            jdbcTemplate.update("DELETE FROM directors WHERE id = ?", id);
            log.info("Режиссер с ID={} удален", director.getId());
        } else {
            throw new DirectorNotFoundException("Режиссер с ID=" + id + " не найден!");
        }
        return director;
    }

    public void delete(Film film) {
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
    }

    public void add(Film film) {
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                        film.getId(), director.getId());
            }
        }
    }

    public Set<Director> getFilmDirectors(Long filmId) {
        String sql = "SELECT director_id, name FROM film_directors" +
                " INNER JOIN directors ON director_id = id WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> new Director(
                rs.getLong("director_id"), rs.getString("name")), filmId
        ));
    }

    public void setDirectorNamesAndSave(Film film) {
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                Director fullDirector = getDirectorById(director.getId());
                director.setName(fullDirector.getName());
            }
            delete(film);
            add(film);
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
        delete(film);
        add(film);
    }
}