package ru.yandex.practicum.filmorate.exceptions;

public class ReviewNotFoundException extends IllegalArgumentException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
