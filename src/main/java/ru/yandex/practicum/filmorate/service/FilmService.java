package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.Genre;
import ru.yandex.practicum.filmorate.enums.MPA;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;


    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public void addLike(long userId, long filmId) {
        userStorage.findById(userId);
        filmStorage.findById(filmId);

        String sql = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, userId, filmId);
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        } catch (Exception e) {
            log.warn("Лайк уже существует или произошла ошибка: {}", e.getMessage());
        }
    }

    public void removeLike(long userId, long filmId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
        int deleted = jdbcTemplate.update(sql, userId, filmId);

        if (deleted == 0) {
            log.error("Лайк пользователя {} фильму {} не найден", userId, filmId);
            throw new NotFoundException("Лайк пользователя с id " + userId +
                    " фильму с id " + filmId + " не найден");
        }

        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getTopPopularFilms(long count) {
        String sql = "SELECT f.*, COUNT(l.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration")
            );
            film.setId(rs.getLong("id"));
            return film;
        }, count);

        for (Film film : films) {
            String genreSql = "SELECT g.name FROM genre g " +
                    "JOIN genres_film gf ON g.id = gf.genre_id " +
                    "WHERE gf.film_id = ?";
            List<String> genres = jdbcTemplate.queryForList(genreSql, String.class, film.getId());
            if (!genres.isEmpty()) {
                film.setGenre(Genre.valueOf(genres.get(0)));
            }

            String mpaSql = "SELECT m.name FROM mpa m " +
                    "JOIN films f ON m.id = f.mpa_id " +
                    "WHERE f.id = ?";
            try {
                String mpaName = jdbcTemplate.queryForObject(mpaSql, String.class, film.getId());
                if (mpaName != null) {
                    film.setMpa(MPA.valueOf(mpaName));
                }
            } catch (Exception e) {
                throw new NotFoundException("Mpa не найден");
            }
        }
        return films;
    }
}