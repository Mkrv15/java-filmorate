package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDbStorage reviewDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    public Review create(Review review) {
        filmDbStorage.getFilmById(review.getFilmId());
        userDbStorage.getUserById(review.getUserId());

        return reviewDbStorage.create(review);
    }

    public Review update(Review review) {
        filmDbStorage.getFilmById(review.getFilmId());
        userDbStorage.getUserById(review.getUserId());

        return reviewDbStorage.update(review);
    }

    public Review delete(Long reviewId) {
        return reviewDbStorage.delete(reviewId);
    }

    public Review findById(Long reviewId) {
        return reviewDbStorage.getReviewById(reviewId);
    }

    public List<Review> findReviewByQuery(Long filmId, Integer count) {
        return reviewDbStorage.getReviewsByQuery(filmId, count);
    }
}
