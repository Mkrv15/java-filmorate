package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    Review delete(Long reviewId);

    Review getReviewById(Long reviewId);

    List<Review> getReviewsByQuery(Long filmId, Integer count);
}
