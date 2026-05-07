package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({LikeDbStorage.class, FilmDbStorage.class, UserDbStorage.class,
        MpaService.class, MpaDbStorage.class, GenreService.class, GenreDbStorage.class})
class LikeDbTests {

    @Autowired
    private LikeDbStorage likeStorage;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private MpaService mpaService;

    private Long filmId;
    private Long userId;

    @BeforeEach
    void setUp() {
        Mpa mpa = mpaService.getMpaById(1);
        Film film = new Film(null, "Test Film", "Description",
                LocalDate.of(2000, 1, 1), 120, new HashSet<>(), mpa, null);
        Film createdFilm = filmStorage.create(film);
        filmId = createdFilm.getId();

        User user = new User(null, "test@example.com", "testuser", "Test User",
                LocalDate.of(1990, 1, 1), null);
        User createdUser = userStorage.create(user);
        userId = createdUser.getId();
    }

    @Test
    public void testAddLike() {
        likeStorage.addLike(filmId, userId);

        List<Long> likes = likeStorage.getLikes(filmId);

        assertThat(likes).contains(userId);
    }

    @Test
    public void testDeleteLike() {
        likeStorage.addLike(filmId, userId);
        likeStorage.deleteLike(filmId, userId);

        List<Long> likes = likeStorage.getLikes(filmId);

        assertThat(likes).doesNotContain(userId);
    }

    @Test
    public void testGetLikes() {
        likeStorage.addLike(filmId, userId);

        List<Long> likes = likeStorage.getLikes(filmId);

        assertThat(likes).isNotEmpty();
        assertThat(likes).hasSize(1);
        assertThat(likes.get(0)).isEqualTo(userId);
    }

    @Test
    public void testGetPopular() {
        likeStorage.addLike(filmId, userId);

        List<Film> popularFilms = likeStorage.getPopular(10);

        assertThat(popularFilms).isNotEmpty();
        assertThat(popularFilms.get(0).getId()).isEqualTo(filmId);
    }
}
