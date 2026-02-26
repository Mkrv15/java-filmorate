package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

@Slf4j
@Getter
@Setter
@ToString
public class Film {
    public static final LocalDate DATE_FIRST_FILM = LocalDate.of(1895, 12, 28);
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    public Film(String name, String description, LocalDate releaseDate, int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        if (!isValid()) {
            throw new ValidationException();
        }
    }

    public boolean isValid() {
        return validateName() && validateDescription() && validateReleaseDate() && validateDuration();
    }

    private boolean validateName() {
        if (!(name == null || name.isBlank())) {
            return true;
        }
        log.error("Название не может быть пустым");
        return false;
    }

    private boolean validateDescription() {
        if (description.length() <= 200) {
            return true;
        }
        log.error("Максимальная длина описания — 200 символов");
        return false;
    }

    private boolean validateReleaseDate() {
        if (releaseDate.isAfter(DATE_FIRST_FILM)) {
            return true;
        }
        log.error("Дата релиза — не раньше 28 декабря 1895 года");
        return false;
    }

    private boolean validateDuration() {
        if (duration > 0) {
            return true;
        }
        log.error("Продолжительность фильма должна быть положительным числом");
        return false;
    }
}
