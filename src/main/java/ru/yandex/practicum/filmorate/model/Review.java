package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class Review {
    private Long id;
    @NotBlank
    private String content;
    private int useful;
    @NotNull
    private Long filmId;
    @NotNull
    private Long userId;
    @NotNull
    private Boolean isPositive;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("content", content);
        values.put("film_id", filmId);
        values.put("user_id", userId);
        values.put("useful", useful);
        values.put("is_positive", isPositive);
        return values;
    }
}
