package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.service.genre.GenreService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LikeDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreService genreService;
    private final DirectorService directorService;

    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Film> getPopular(Integer count) {
        String sql = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id,
                   r.id AS rating_id, r.name AS rating_name
            FROM films f
            LEFT JOIN ratings_mpa r ON f.rating_id = r.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.id, r.name
            ORDER BY COUNT(fl.user_id) DESC
            LIMIT ? """;

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Mpa mpa = new Mpa(rs.getInt("rating_id"), rs.getString("rating_name"));
            return Film.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .duration(rs.getInt("duration"))
                    .mpa(mpa)
                    .build();
        }, count);

        enrich(films);
        return films;
    }

    public List<Film> getPopularByGenreAndYear(Integer count, Integer genreId, Integer year) {
        List<Film> films;

        if (year == null && genreId != null) {
            String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id,
                       r.id AS rating_id, r.name AS rating_name
                FROM films f
                LEFT JOIN ratings_mpa r ON f.rating_id = r.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                INNER JOIN film_genres fg ON f.id = fg.film_id
                WHERE fg.genre_id = ?
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.id, r.name
                ORDER BY COUNT(fl.user_id) DESC
                LIMIT ?
            """;
            films = jdbcTemplate.query(sql, this::mapFilmWithoutDetails, genreId, count);

        } else if (genreId == null && year != null) {
            String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id,
                       r.id AS rating_id, r.name AS rating_name
                FROM films f
                LEFT JOIN ratings_mpa r ON f.rating_id = r.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                WHERE EXTRACT(YEAR FROM f.release_date) = ?
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.id, r.name
                ORDER BY COUNT(fl.user_id) DESC
                LIMIT ?
            """;
            films = jdbcTemplate.query(sql, this::mapFilmWithoutDetails, year, count);

        } else {
            String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id,
                       r.id AS rating_id, r.name AS rating_name
                FROM films f
                LEFT JOIN ratings_mpa r ON f.rating_id = r.id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                INNER JOIN film_genres fg ON f.id = fg.film_id
                WHERE fg.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ?
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.id, r.name
                ORDER BY COUNT(fl.user_id) DESC
                LIMIT ?
            """;
            films = jdbcTemplate.query(sql, this::mapFilmWithoutDetails, genreId, year, count);
        }

        enrich(films);
        return films;
    }

    public List<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }

    public Map<Long, Set<Long>> getLikesBatch(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" +
                filmIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";

        Map<Long, Set<Long>> likesMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            likesMap.computeIfAbsent(filmId, k -> new HashSet<>())
                    .add(rs.getLong("user_id"));
        });

        return likesMap;
    }

    private Film mapFilmWithoutDetails(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa(rs.getInt("rating_id"), rs.getString("rating_name"));
        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .build();
    }

    private void enrich(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, Set<Long>> likesMap = getLikesBatch(filmIds);
        Map<Long, Set<Genre>> genresMap = genreService.getFilmGenresBatch(filmIds);
        Map<Long, Set<Director>> directorsMap = directorService.getFilmDirectorsBatch(filmIds);

        for (Film film : films) {
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setGenres(genresMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirectors(directorsMap.getOrDefault(film.getId(),new HashSet<>()));
        }
    }
}