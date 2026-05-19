package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaService.class, MpaDbStorage.class,
        GenreService.class, GenreDbStorage.class, LikeDbStorage.class,
        DirectorService.class, DirectorDbStorage.class})
class FilmDbTests {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private MpaService mpaService;

    @Test
    public void testFindFilmByIdNotFound() {
        assertThatThrownBy(() -> filmStorage.getFilmById(999L))
                .isInstanceOf(FilmNotFoundException.class);
    }

    @Test
    public void testCreateFilm() {
        Mpa mpa = mpaService.getMpaById(1);
        Film film = new Film(null, "New Film", "New Description",
                LocalDate.of(2020, 5, 5), 120, new HashSet<>(), mpa, null, null);
        Film created = filmStorage.create(film);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(created.getId()));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(filmFound ->
                        assertThat(filmFound).hasFieldOrPropertyWithValue("name", "New Film")
                );
    }

    @Test
    public void testUpdateFilm() {
        Mpa mpa = mpaService.getMpaById(1);
        Film film = new Film(null, "Old Film", "Old Description",
                LocalDate.of(2000, 1, 1), 100, new HashSet<>(), mpa, null, null);
        Film created = filmStorage.create(film);

        created.setName("Updated Film");
        created.setDuration(150);
        created.setDescription("Updated Description");

        filmStorage.update(created);

        Film filmFromDb = filmStorage.getFilmById(created.getId());

        assertThat(filmFromDb).hasFieldOrPropertyWithValue("name", "Updated Film");
        assertThat(filmFromDb).hasFieldOrPropertyWithValue("duration", 150);
        assertThat(filmFromDb).hasFieldOrPropertyWithValue("description", "Updated Description");
    }

    @Test
    public void testDeleteFilm() {
        Mpa mpa = mpaService.getMpaById(1);
        Film film = new Film(null, "Delete Film", "Delete Description",
                LocalDate.of(2000, 1, 1), 100, new HashSet<>(), mpa, null, null);
        Film created = filmStorage.create(film);

        filmStorage.delete(created.getId());

        assertThatThrownBy(() -> filmStorage.getFilmById(created.getId()))
                .isInstanceOf(FilmNotFoundException.class);
    }

    @Test
    public void testGetAllFilms() {
        Mpa mpa = mpaService.getMpaById(1);
        filmStorage.create(new Film(null, "Film A", "Desc A",
                LocalDate.of(2000, 1, 1), 100, new HashSet<>(), mpa, null, null));
        filmStorage.create(new Film(null, "Film B", "Desc B",
                LocalDate.of(2001, 2, 2), 110, new HashSet<>(), mpa, null, null));

        assertThat(filmStorage.getFilms().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void testGetFilmById() {
        Mpa mpa = mpaService.getMpaById(1);
        Film film = new Film(null, "Test Film", "Test Description",
                LocalDate.of(2010, 1, 1), 90, new HashSet<>(), mpa, null, null);
        Film created = filmStorage.create(film);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(created.getId()));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(filmFound ->
                        assertThat(filmFound).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }
}