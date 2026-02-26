package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UsersValidationTests {

    @Test
    void testEmail() {
        Assertions.assertDoesNotThrow(() -> {
            new User("email@yandex.ru", "login", "name"
                    , LocalDate.of(2000, 10, 15));
        });

        Assertions.assertThrows(ValidationException.class, () -> {
            new User("emailyandex.ru", "login", "name"
                    , LocalDate.of(2000, 10, 15));
        });

        Assertions.assertThrows(ValidationException.class, () -> {
            new User(null, "login", "name"
                    , LocalDate.of(2000, 10, 15));
        });

        Assertions.assertThrows(ValidationException.class, () -> {
            new User("", "login", "name"
                    , LocalDate.of(2000, 10, 15));
        });
    }

    @Test
    void testLogin() {
        Assertions.assertDoesNotThrow(() -> {
            new User("email@yandex.ru", "login", "name"
                    , LocalDate.of(2000, 10, 15));
        });

        Assertions.assertThrows(ValidationException.class, () -> {
            new User("email@yandex.ru", null, "name"
                    , LocalDate.of(2000, 10, 15));
        });

        Assertions.assertThrows(ValidationException.class, () -> {
            new User("email@yandex.ru", "", "name"
                    , LocalDate.of(2000, 10, 15));
        });

        Assertions.assertThrows(ValidationException.class, () -> {
            new User("email@yandex.ru", "lo gin", "name"
                    , LocalDate.of(2000, 10, 15));
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    void testBirthdayReturnDoesNotThrow(int days) {
        Assertions.assertDoesNotThrow(() -> {
            new User("email@yandex.ru", "login", "name"
                    , LocalDate.now().minusDays(days));
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    void testBirthdayReturnThrow(int days) {
        Assertions.assertThrows(ValidationException.class, () -> {
            new User("email@yandex.ru", "login", "name"
                    , LocalDate.now().plusDays(days));
        });
    }
}
