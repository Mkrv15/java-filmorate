package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewLikeDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Optional<Boolean> findRating(Long reviewId, Long userId) {
        return jdbcTemplate.query(
                        "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?",
                        (rs, rowNum) -> rs.getBoolean("is_like"),
                        reviewId, userId)
                .stream()
                .findFirst();
    }

    public void insert(Long reviewId, Long userId, boolean isLike) {
        jdbcTemplate.update(
                "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)",
                reviewId, userId, isLike);
    }

    public void delete(Long reviewId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?",
                reviewId, userId);
    }

    public void adjustUseful(Long reviewId, int delta) {
        jdbcTemplate.update(
                "UPDATE reviews SET useful = useful + ? WHERE id = ?",
                delta, reviewId);
    }
}
