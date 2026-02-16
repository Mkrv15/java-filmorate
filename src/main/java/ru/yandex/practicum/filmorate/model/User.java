package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
@Slf4j
@Data
@Builder
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

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
