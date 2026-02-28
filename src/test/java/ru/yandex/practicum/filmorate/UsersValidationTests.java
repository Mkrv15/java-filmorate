package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

public class UsersValidationTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private User user;

    @Test
    void testEmail() {
        user = new User("email@yandex.ru", "login", "name",
                LocalDate.of(2000, 10, 15));
        Assertions.assertTrue(validator.validate(user).isEmpty());

        user = new User("emailyandex.ru", "login", "name",
                LocalDate.of(2000, 10, 15));
        Assertions.assertFalse(validator.validate(user).isEmpty());

        user = new User(null, "login", "name",
                LocalDate.of(2000, 10, 15));
        Assertions.assertFalse(validator.validate(user).isEmpty());

        user = new User("", "login", "name",
                LocalDate.of(2000, 10, 15));
        Assertions.assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void testLogin() {
        user = new User("email@yandex.ru", "login", "name",
                LocalDate.of(2000, 10, 15));
        Assertions.assertTrue(validator.validate(user).isEmpty());

        user = new User("email@yandex.ru", "", "name",
                LocalDate.of(2000, 10, 15));
        Assertions.assertFalse(validator.validate(user).isEmpty());

        user = new User("email@yandex.ru", null, "name",
                LocalDate.of(2000, 10, 15));
        Assertions.assertFalse(validator.validate(user).isEmpty());

    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    void testBirthdayReturnTrue(int days) {
        user = new User("email@yandex.ru", "login", "name",
                LocalDate.now().minusDays(days));
        Assertions.assertTrue(validator.validate(user).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10})
    void testBirthdayReturnThrow(int days) {
        user = new User("email@yandex.ru", "login", "name",
                LocalDate.now().plusDays(days));
        Assertions.assertFalse(validator.validate(user).isEmpty());
    }
}
