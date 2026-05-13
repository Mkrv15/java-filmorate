package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikeDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
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
        String getPopularQuery = "SELECT id, name, description, release_date, duration, rating_id " +
                "FROM films LEFT JOIN film_likes ON films.id = film_likes.film_id " +
                "GROUP BY films.id ORDER BY COUNT(film_likes.user_id) DESC LIMIT ?";

        return jdbcTemplate.query(getPopularQuery, this::mapFilm, count);
    }

    public List<Film> getPopularByGenreAndYear(Integer count, Integer genreId, Integer year) {
        if (year == null) {
            String getPopularQueryByGenre = "SELECT id, name, description, release_date, duration, rating_id " +
                    "FROM films AS f " +
                    "LEFT JOIN film_likes AS fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                    "WHERE fg.genre_id = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY COUNT(fl.user_id) DESC ";

            return jdbcTemplate.query(getPopularQueryByGenre, this::mapFilm, genreId);
        } else if (genreId == null) {
            String getPopularQueryByYear = "SELECT id, name, description, release_date, duration, rating_id " +
                    "FROM films AS f " +
                    "LEFT JOIN film_likes AS fl ON f.id = fl.film_id " +
                    "WHERE EXTRACT(YEAR FROM release_date) = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY COUNT(fl.user_id) DESC ";

            return jdbcTemplate.query(getPopularQueryByYear, this::mapFilm, year);
        } else {
            String getPopularByGenreAndYear = "SELECT id, name, description, release_date, duration, rating_id " +
                    "FROM films AS f " +
                    "LEFT JOIN film_likes AS fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                    "WHERE fg.genre_id = ? " +
                    "AND EXTRACT(YEAR FROM release_date) = ? " +
                    "GROUP BY f.id " +
                    "ORDER BY COUNT(fl.user_id) DESC LIMIT ? ";

            return jdbcTemplate.query(getPopularByGenreAndYear, this::mapFilm, genreId, year, count);
        }
    }

    public List<Long> getLikes(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }

    private Film mapFilm(ResultSet rs, int rowNum) throws SQLException {
        return new Film(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_Date").toLocalDate(),
                rs.getInt("duration"),
                new HashSet<>(getLikes(rs.getLong("id"))),
                mpaService.getMpaById(rs.getInt("rating_id")),
                genreService.getFilmGenres(rs.getLong("id")),
                directorService.getFilmDirectors(rs.getLong("id")));
    }
}