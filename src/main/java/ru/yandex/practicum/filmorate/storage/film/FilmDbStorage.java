package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final LikeDbStorage likeDbStorage;
    private final DirectorDbStorage directorDbStorage;

    @Override
    public List<Film> getFilms() {
        String filmsSql = "SELECT f.*, r.id as rating_id, r.name as rating_name " +
                "FROM films f " +
                "LEFT JOIN ratings_mpa r ON f.rating_id = r.id";
        List<Film> films = jdbcTemplate.query(filmsSql, this::mapFilm);
        enrich(films);
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
        film.setMpa(mpaDbStorage.getMpaById(film.getMpa().getId()));

        genreDbStorage.setGenreNamesAndSave(film);
        directorDbStorage.setDirectorNamesAndSave(film);
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
            film.setMpa(mpaDbStorage.getMpaById(film.getMpa().getId()));
            genreDbStorage.updateFilmGenres(film);
            directorDbStorage.updateFilmDirectors(film);
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
            Mpa mpa = mpaDbStorage.getMpaById(filmRows.getInt("rating_id"));
            Set<Genre> genres = genreDbStorage.getFilmGenres(filmId);
            Set<Director> directors = directorDbStorage.getFilmDirectors(filmId);
            film = new Film(
                    filmRows.getLong("id"),
                    filmRows.getString("name"),
                    filmRows.getString("description"),
                    filmRows.getDate("release_date").toLocalDate(),
                    filmRows.getInt("duration"),
                    new HashSet<>(likeDbStorage.getLikes(filmRows.getLong("id"))),
                    mpa,
                    genres,
                    directors);
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

    public List<Film> getRecommendations(Long userId) {
        if (userId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        String sql = "SELECT f2.user_id " +
                "FROM film_likes AS f1 " +
                "JOIN film_likes AS f2 ON f1.film_id = f2.film_id " +
                "WHERE f1.user_id = ? AND f2.user_id != ? " +
                "GROUP BY f2.user_id " +
                "ORDER BY COUNT(DISTINCT f2.film_id) DESC " +
                "LIMIT 1";

        List<Long> userIds = jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getLong("user_id"), userId, userId);

        if (userIds.isEmpty()) {
            return List.of();
        }
        Long similarUserId = userIds.getFirst();

        List<Film> userFilms = getFilmsFromUser(userId);
        List<Film> similarUserFilms = getFilmsFromUser(similarUserId);
        similarUserFilms.removeAll(userFilms);

        return similarUserFilms;
    }

    public List<Film> getFilmsFromUser(Long userId) {
        String filmsSql = "SELECT f.*, r.id as rating_id, r.name as rating_name " +
                "FROM films f " +
                "LEFT JOIN ratings_mpa r ON f.rating_id = r.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE fl.user_id = ?";
        List<Film> films = jdbcTemplate.query(filmsSql, this::mapFilm, userId);
        enrich(films);
        return films;
    }

    public List<Film> getFilmsByYear(Long directorId) {
        String sql = "SELECT id, name, description, release_date, duration, rating_id " +
                "FROM films " +
                "LEFT JOIN film_directors ON films.id = film_directors.film_id " +
                "WHERE film_directors.director_id = ? " +
                "GROUP BY films.id " +
                "ORDER BY release_date";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("release_Date").toLocalDate(),
                        rs.getInt("duration"),
                        new HashSet<>(likeDbStorage.getLikes(rs.getLong("id"))),
                        mpaDbStorage.getMpaById(rs.getInt("rating_id")),
                        genreDbStorage.getFilmGenres(rs.getLong("id")),
                        directorDbStorage.getFilmDirectors(rs.getLong("id"))),
                directorId
        );
    }

    public List<Film> getFilmsByLikes(Long directorId) {
        String sql = "SELECT id, name, description, release_date, duration, rating_id " +
                "FROM films " +
                "LEFT JOIN film_directors ON films.id = film_directors.film_id " +
                "LEFT JOIN film_likes ON films.id = film_likes.film_id " +
                "WHERE film_directors.director_id = ? " +
                "GROUP BY films.id " +
                "ORDER BY COUNT(film_likes.user_id) DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Film(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("release_Date").toLocalDate(),
                        rs.getInt("duration"),
                        new HashSet<>(likeDbStorage.getLikes(rs.getLong("id"))),
                        mpaDbStorage.getMpaById(rs.getInt("rating_id")),
                        genreDbStorage.getFilmGenres(rs.getLong("id")),
                        directorDbStorage.getFilmDirectors(rs.getLong("id"))),
                directorId
        );
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

    private Film mapFilm(ResultSet rs, int rowNum) throws SQLException {
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
    }

    private void enrich(List<Film> films) {
        if (films.isEmpty()) return;

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
    }
}