package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Review create(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("id");

        review.setId(simpleJdbcInsert.executeAndReturnKey(review.toMap()).longValue());

        return review;
    }

    public Review update(Review review) {
        if (review == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }

        String sqlQuery = "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ?, FILM_ID = ?, USER_ID = ? WHERE id = ?";
        if (jdbcTemplate.update(sqlQuery, review.getContent(), review.getIsPositive(), review.getFilmId(), review.getUserId(), review.getId()) != 0) {
            return review;
        } else {
            throw new ReviewNotFoundException("Отзыв с ID=" + review.getId() + " не найден!");
        }
    }

    public List<Review> getReviewsByQuery(Long filmId, Integer count) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM REVIEWS"
        );

        List<Object> params = new ArrayList<>();
        boolean searchByFilm = filmId != null;

        if (searchByFilm) {
            sql.append(" WHERE FILM_ID = ?");
            params.add(filmId);
        }

        if (count != null && count > 0) {
            sql.append(" LIMIT ?");
            params.add(count);
        }

        return jdbcTemplate.query(sql.toString(), this::mapReview, params.toArray());
    }

    private Review mapReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .id(rs.getLong("id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .filmId(rs.getLong("film_id"))
                .userId(rs.getLong("user_id"))
                .useful(rs.getInt("useful"))
                .build();
    }

    public Review getReviewById(Long reviewId) {
        if (reviewId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Review review;
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT * FROM REVIEWS WHERE id = ?", reviewId);
        if (reviewRows.first()) {
            review = new Review(
                    reviewRows.getLong("id"),
                    reviewRows.getString("content"),
                    reviewRows.getInt("useful"),
                    reviewRows.getLong("film_id"),
                    reviewRows.getLong("user_id"),
                    reviewRows.getBoolean("is_positive")
            );
        } else {
            throw new ReviewNotFoundException("Отзыв с ID=" + reviewId + " не найден!");
        }

        return review;
    }

    public Review delete(Long reviewId) {
        if (reviewId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Review review = getReviewById(reviewId);
        if (jdbcTemplate.update("DELETE FROM REVIEWS WHERE id = ? ", reviewId) == 0) {
            throw new ReviewNotFoundException("Отзыв с ID=" + reviewId + " не найден!");
        }
        return review;
    }
}
