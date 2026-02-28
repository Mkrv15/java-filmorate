package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {
    private int id;

    @NotNull(message = "Почта не может быть null")
    @NotBlank(message = "Почта не может быть пустой")
    @Email(message = "Неверный формат почты")
    private final String email;

    @NotBlank(message = "Логин не может быть пустым")
    @NotNull(message = "Логин не может быть null")
    private final String login;

    private final String name;

    @NotNull(message = "День рождения не может быть null")
    @Past(message = "День рождения не может быть в будущем")
    private final LocalDate birthday;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
        if (name == null || name.isBlank()) {
            this.name = login;
            log.debug("Пользователю {} в качестве имени присвоен логин", name);
        } else {
            this.name = name;
        }
    }
}
