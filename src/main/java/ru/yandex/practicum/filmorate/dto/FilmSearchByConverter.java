package ru.yandex.practicum.filmorate.dto;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FilmSearchByConverter implements Converter<String, FilmSearchBy> {
    @Override
    public FilmSearchBy convert(String source) {
        return FilmSearchBy.valueOf(source.trim().toUpperCase());
    }
}