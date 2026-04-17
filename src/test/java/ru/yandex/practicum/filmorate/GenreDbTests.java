package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
class GenreDbTests {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    public void testFindGenreByIdNotFound() {
        assertThatThrownBy(() -> genreStorage.getGenreById(999))
                .isInstanceOf(GenreNotFoundException.class);
    }

    @Test
    public void testGetGenreById() {
        Optional<Genre> genreOptional = Optional.ofNullable(genreStorage.getGenreById(1));

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    public void testGetAllGenres() {
        assertThat(genreStorage.getGenres()).hasSize(6);
    }

    @Test
    public void testGetGenreByIdWithDifferentIds() {
        for (int i = 1; i <= 6; i++) {
            Genre genre = genreStorage.getGenreById(i);
            assertThat(genre).isNotNull();
            assertThat(genre.getId()).isEqualTo(i);
            assertThat(genre.getName()).isNotEmpty();
        }
    }
}
