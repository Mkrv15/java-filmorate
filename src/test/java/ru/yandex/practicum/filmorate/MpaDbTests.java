package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
class MpaDbTests {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    public void testFindMpaByIdNotFound() {
        assertThatThrownBy(() -> mpaStorage.getMpaById(999))
                .isInstanceOf(MpaNotFoundException.class);
    }

    @Test
    public void testGetMpaById() {
        Optional<Mpa> mpaOptional = Optional.ofNullable(mpaStorage.getMpaById(1));

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    public void testGetAllMpa() {
        Collection<Mpa> mpas = mpaStorage.getAllMpa();

        assertThat(mpas).isNotNull();
    }

    @Test
    public void testGetMpaByIdWithDifferentIds() {
        for (int i = 1; i <= 5; i++) {
            Mpa mpa = mpaStorage.getMpaById(i);
            assertThat(mpa).isNotNull();
            assertThat(mpa.getId()).isEqualTo(i);
            assertThat(mpa.getName()).isNotEmpty();
        }
    }
}
