package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final LikeDbStorage likeDbStorage;

    @Override
    public List<Film> getFilms() {
        String filmsSql = "SELECT f.*, r.id as rating_id, r.name as rating_name " +
                "FROM films f " +
                "LEFT JOIN ratings_mpa r ON f.rating_id = r.id";
        List<Film> films = jdbcTemplate.query(filmsSql, (rs, rowNum) -> {
            Mpa mpa = new Mpa(rs.getInt("rating_id"), rs.getString("rating_name"));

            return Film.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .duration(rs.getInt("duration"))
                    .mpa(mpa)
                    .likes(new HashSet<>())
                    .genres(new HashSet<>())
                    .build();
        });
        if (films.isEmpty()) return films;

        List<Long> filmsId = films.stream().map(Film::getId).toList();
        String ids = filmsId.stream().map(String::valueOf).collect(Collectors.joining(","));

        Map<Long, Set<Long>> likesMap = new HashMap<>();
        String likesSql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + ids + ")";
        jdbcTemplate.query(likesSql, rs -> {
            Long filmId = rs.getLong("film_id");
            likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(rs.getLong("user_id"));
        });

        Map<Long, Set<Genre>> genresMap = new HashMap<>();
        String genresSql = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + ids + ") " +
                "ORDER BY fg.film_id, g.id";
        jdbcTemplate.query(genresSql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            genresMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        });

        for (Film film : films) {
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
            Set<Genre> genres = genresMap.get(film.getId());
            film.setGenres(genres == null || genres.isEmpty() ? null : genres);
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
        } else if (film.getMpa().getId() > 5) {
            throw new MpaNotFoundException("Mpa с id " + film.getMpa().getId() + "не найден.");
        }
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        film.setId(simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue());
        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genre.setName(genreService.getGenreById(genre.getId()).getName());
            }
            genreService.putGenres(film);
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
        } else if (film == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        String sqlQuery = "UPDATE films SET " +
                "name = ?, description = ?, release_date = ?, duration = ?, " +
                "rating_id = ? WHERE id = ?";
        if (jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()) != 0) {
            film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
            if (film.getGenres() != null) {
                Collection<Genre> sortGenres = film.getGenres().stream()
                        .sorted(Comparator.comparing(Genre::getId))
                        .collect(Collectors.toList());
                film.setGenres(new LinkedHashSet<>(sortGenres));
                for (Genre genre : film.getGenres()) {
                    genre.setName(genreService.getGenreById(genre.getId()).getName());
                }
            }
            genreService.putGenres(film);
            return film;
        } else {
            throw new FilmNotFoundException("Фильм с ID=" + film.getId() + " не найден!");
        }
    }

    @Override
    public Film getFilmById(Long filmId) {
        if (filmId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Film film;
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", filmId);
        if (filmRows.first()) {
            Mpa mpa = mpaService.getMpaById(filmRows.getInt("rating_id"));
            Set<Genre> genres = genreService.getFilmGenres(filmId);
            film = new Film(
                    filmRows.getLong("id"),
                    filmRows.getString("name"),
                    filmRows.getString("description"),
                    filmRows.getDate("release_date").toLocalDate(),
                    filmRows.getInt("duration"),
                    new HashSet<>(likeDbStorage.getLikes(filmRows.getLong("id"))),
                    mpa,
                    genres);
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата выхода фильма не может быть раньше 28.12.1895");
            }
        } else {
            throw new FilmNotFoundException("Фильм с ID=" + filmId + " не найден!");
        }
        if (film.getGenres().isEmpty()) {
            film.setGenres(null);
        }
        return film;
    }

    @Override
    public Film delete(Long filmId) {
        if (filmId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Film film = getFilmById(filmId);
        String sqlQuery = "DELETE FROM films WHERE id = ? ";
        if (jdbcTemplate.update(sqlQuery, filmId) == 0) {
            throw new FilmNotFoundException("Фильм с ID=" + filmId + " не найден!");
        }
        return film;
    }
}