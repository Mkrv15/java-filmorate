package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Review {
    @JsonProperty("reviewId")
    private Long id;
    @NotBlank
    private String content;
    private Integer useful;
    @NotNull
    private Long filmId;
    @NotNull
    private Long userId;
    @NotNull
    private Boolean isPositive;
}
