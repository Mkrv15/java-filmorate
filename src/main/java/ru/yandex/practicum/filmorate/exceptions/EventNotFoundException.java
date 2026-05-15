package ru.yandex.practicum.filmorate.exceptions;

public class EventNotFoundException extends IllegalArgumentException {
    public EventNotFoundException(String s) {
        super(s);
    }
}
