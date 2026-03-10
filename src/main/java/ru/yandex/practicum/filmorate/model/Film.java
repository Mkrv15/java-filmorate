package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
@ToString
public class Film {
    public static final LocalDate DATE_FIRST_FILM = LocalDate.of(1895, 12, 28);
    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Long> likes;

    public Film(String name, String description, LocalDate releaseDate, int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = new HashSet<>();
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
