package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmsValidationTests {
    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film("name", "description"
                , LocalDate.of(2000, 10, 15), 120);
    }

    @Test
    void testFilmName() {
        Assertions.assertTrue(film.isValid());

        film.setName("");
        Assertions.assertFalse(film.isValid());

        film.setName(null);
        Assertions.assertFalse(film.isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 100})
    void testDescriptionReturnTrue(int count) {
        film.setDescription("f".repeat(count));
        Assertions.assertTrue(film.isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {201, 400})
    void testDescriptionReturnFalse(int count) {
        film.setDescription("f".repeat(count));
        Assertions.assertFalse(film.isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 300})
    void testReleaseDateReturnTrue(int count) {
        film.setReleaseDate(Film.DATE_FIRST_FILM.plusDays(count));
        Assertions.assertTrue(film.isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 300})
    void testReleaseDateReturnFalse(int count) {
        film.setReleaseDate(Film.DATE_FIRST_FILM.minusDays(count));
        Assertions.assertFalse(film.isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10})
    void testDurationReturnFalse(int duration) {
        film.setDuration(duration);
        Assertions.assertFalse(film.isValid());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    void testDurationReturnTrue(int duration) {
        film.setDuration(duration);
        Assertions.assertTrue(film.isValid());
    }
}
