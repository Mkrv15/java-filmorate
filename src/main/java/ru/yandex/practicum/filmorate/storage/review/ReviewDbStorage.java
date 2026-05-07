package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> values = new HashMap<>();
        values.put("content", review.getContent());
        values.put("film_id", review.getFilmId());
        values.put("user_id", review.getUserId());
        values.put("is_positive", review.getIsPositive());
        values.put("useful", 0);

        review.setId(simpleJdbcInsert.executeAndReturnKey(values).longValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sqlQuery = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getId());
        if (updated == 0) {
            throw new ReviewNotFoundException("Отзыв с ID=" + review.getId() + " не найден!");
        }
        return getReviewById(review.getId());
    }

    @Override
    public List<Review> getReviewsByQuery(Long filmId, Integer count) {
        StringBuilder sql = new StringBuilder("SELECT * FROM reviews");
        List<Object> params = new ArrayList<>();

        if (filmId != null) {
            sql.append(" WHERE film_id = ?");
            params.add(filmId);
        }
        sql.append(" ORDER BY useful DESC");
        if (count != null) {
            sql.append(" LIMIT ?");
            params.add(count);
        }

        return jdbcTemplate.query(sql.toString(), this::mapReview, params.toArray());
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return jdbcTemplate.query("SELECT * FROM reviews WHERE id = ?", this::mapReview, reviewId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ReviewNotFoundException("Отзыв с ID=" + reviewId + " не найден!"));
    }

    @Override
    public Review delete(Long reviewId) {
        Review review = getReviewById(reviewId);
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", reviewId);
        return review;
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
}
