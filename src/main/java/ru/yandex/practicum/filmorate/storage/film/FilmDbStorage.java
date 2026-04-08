package ru.yandex.practicum.filmorate.storage.film;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.Genre;
import ru.yandex.practicum.filmorate.enums.MPA;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage{
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE id = ?";


    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1,film.getName());
            ps.setString(2,film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4,film.getDuration());
            ps.setInt(5, film.getMpa() != null ? film.getMpa().ordinal() + 1 : null);
            return ps;
        }, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);

        if (film.getGenre() != null) {
            saveFilmGenre(id, film.getGenre().ordinal() + 1);
        }

        log.info("В БД добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film delete(Long id) {
        Film film = findById(id);

        String deleteGenresSql = "DELETE FROM genres_film WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, id);

        String deleteLikesSql = "DELETE FROM likes WHERE film_id = ?";
        jdbcTemplate.update(deleteLikesSql, id);

        String deleteFilmSql = "DELETE FROM films WHERE id = ?";
        int deleted = jdbcTemplate.update(deleteFilmSql, id);

        if (deleted == 0) {
            log.error("Фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }

        log.info("Фильм {} удален из БД", film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        int updated = jdbcTemplate.update(UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                newFilm.getMpa() != null ? newFilm.getMpa().ordinal() + 1 : null,
                newFilm.getId());

        if (updated == 0){
            log.error("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        String deleteGenresSql = "DELETE FROM genres_film WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql,newFilm.getId());

        if (newFilm.getGenre() != null){
            saveFilmGenre(newFilm.getId(),newFilm.getGenre().ordinal() + 1);
        }

        log.info("Фильм {} обновлен в БД", newFilm);
        return findById(newFilm.getId());
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_QUERY,filmMapper);

        films.forEach(this::loadFilmGenre);
        return films;
    }

    @Override
    public Film findById(Long id) {
        try{
            Film film = jdbcTemplate.queryForObject(FIND_BY_ID_QUERY,filmMapper,id);
            loadFilmGenre(film);
            loadFilmLikes(film);
            loadFilmMpa(film);
            return film;
        } catch (EmptyResultDataAccessException e){
            log.error("Фильм с id = {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }

    private void saveFilmGenre(Long filmId, Integer genreId){
        String sql = "INSERT INTO genres_film (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.update(sql,filmId,genreId);
        log.debug("Для фильма {} сохранен жанр с id {}", filmId, genreId);
    }

    private void loadFilmGenre(Film film) {
        String sql = "SELECT g.id, g.name FROM genre g " +
                "JOIN genres_film gf ON g.id = gf.genre_id " +
                "WHERE gf.film_id = ?";

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int genreId = rs.getInt("id");
            String genreName = rs.getString("name");
            return Genre.valueOf(genreName);
        }, film.getId());

        if (!genres.isEmpty()) {
            film.setGenre(genres.get(0));
            log.debug("Для фильма {} загружен жанр: {}", film.getId(), genres.get(0));
        } else {
            film.setGenre(null);
            log.debug("Для фильма {} жанры не найдены", film.getId());
        }
    }

    private void loadFilmLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(sql, Long.class, film.getId());
        film.getLikes().clear();
        if (likes != null && !likes.isEmpty()) {
            film.getLikes().addAll(likes);
        }
        log.debug("Для фильма {} загружено {} лайков", film.getId(), film.getLikes().size());
    }

    private void loadFilmMpa(Film film) {
        String sql = "SELECT m.name FROM mpa m " +
                "JOIN films f ON m.id = f.mpa_id " +
                "WHERE f.id = ?";

        try {
            String mpaName = jdbcTemplate.queryForObject(sql, String.class, film.getId());
            if (mpaName != null) {
                MPA mpa = MPA.valueOf(mpaName);
                film.setMpa(mpa);
                log.debug("Для фильма {} загружен MPA: {}", film.getId(), mpa);
            }
        } catch (EmptyResultDataAccessException e) {
            film.setMpa(null);
            log.debug("Для фильма {} MPA не найден", film.getId());
        }
    }
}
