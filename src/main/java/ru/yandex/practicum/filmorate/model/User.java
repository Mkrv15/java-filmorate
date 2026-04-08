package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.enums.Status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {
    private Long id;

    @NotNull(message = "Почта не может быть null")
    @NotBlank(message = "Почта не может быть пустой")
    @Email(message = "Неверный формат почты")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @NotNull(message = "Логин не может быть null")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "День рождения не может быть null")
    @Past(message = "День рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Long> friends;
    private Status status;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
        this.friends = new HashSet<>();
        if (name == null || name.isBlank()) {
            this.name = login;
            log.debug("Пользователю {} в качестве имени присвоен логин", login);
        } else {
            this.name = name;
        }
    }

    public User() {
    }
}
