package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {
    private int id;
    private final String email;
    private final String login;
    private final String name;
    private final LocalDate birthday;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
        if (isValid()) {
            if (name == null || name.isBlank()) {
                this.name = login;
                log.debug("Пользователю {} в качестве имени присвоен логин", name);
            } else {
                this.name = name;
            }
        } else {
            throw new ValidationException();
        }
    }

    public boolean isValid() {
        return validateEmail() && validateLogin() && validateBirthday();
    }

    private boolean validateEmail() {
        if (!(email == null || email.isBlank()) && email.contains("@")) {
            return true;
        }
        log.error("Электронная почта не может быть пустой и должна содержать символ @");
        return false;
    }

    private boolean validateLogin() {
        if (!(login == null || login.isBlank() || login.contains(" "))) {
            return true;
        }
        log.error("Логин не может быть пустым и содержать пробелы");
        return false;
    }

    private boolean validateBirthday() {
        if (birthday.isBefore(LocalDate.now())) {
            return true;
        }
        log.error("Дата рождения не может быть в будущем");
        return false;
    }
}
