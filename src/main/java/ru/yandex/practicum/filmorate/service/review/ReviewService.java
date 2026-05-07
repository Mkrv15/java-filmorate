package ru.yandex.practicum.filmorate.service.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewLikeDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final ReviewLikeDbStorage reviewLikeStorage;

    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         ReviewLikeDbStorage reviewLikeStorage) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.reviewLikeStorage = reviewLikeStorage;
    }

    public Review create(Review review) {
        filmStorage.getFilmById(review.getFilmId());
        userStorage.getUserById(review.getUserId());
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        filmStorage.getFilmById(review.getFilmId());
        userStorage.getUserById(review.getUserId());
        return reviewStorage.update(review);
    }

    public Review delete(Long reviewId) {
        return reviewStorage.delete(reviewId);
    }

    public Review findById(Long reviewId) {
        return reviewStorage.getReviewById(reviewId);
    }

    public List<Review> findReviewByQuery(Long filmId, Integer count) {
        return reviewStorage.getReviewsByQuery(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        rate(reviewId, userId, true);
    }

    public void addDislike(Long reviewId, Long userId) {
        rate(reviewId, userId, false);
    }

    public void removeLike(Long reviewId, Long userId) {
        unrate(reviewId, userId, true);
    }

    public void removeDislike(Long reviewId, Long userId) {
        unrate(reviewId, userId, false);
    }

    private void rate(Long reviewId, Long userId, boolean isLike) {
        reviewStorage.getReviewById(reviewId);
        userStorage.getUserById(userId);
        Optional<Boolean> current = reviewLikeStorage.findRating(reviewId, userId);
        if (current.isPresent()) {
            if (current.get() == isLike) {
                return;
            }
            reviewLikeStorage.delete(reviewId, userId);
            reviewLikeStorage.insert(reviewId, userId, isLike);
            reviewLikeStorage.adjustUseful(reviewId, isLike ? 2 : -2);
        } else {
            reviewLikeStorage.insert(reviewId, userId, isLike);
            reviewLikeStorage.adjustUseful(reviewId, isLike ? 1 : -1);
        }
    }

    private void unrate(Long reviewId, Long userId, boolean expectedIsLike) {
        reviewStorage.getReviewById(reviewId);
        userStorage.getUserById(userId);
        Optional<Boolean> current = reviewLikeStorage.findRating(reviewId, userId);
        if (current.isEmpty() || current.get() != expectedIsLike) {
            return;
        }
        reviewLikeStorage.delete(reviewId, userId);
        reviewLikeStorage.adjustUseful(reviewId, expectedIsLike ? -1 : 1);
    }
}
