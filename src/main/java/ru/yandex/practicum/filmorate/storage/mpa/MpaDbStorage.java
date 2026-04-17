package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM ratings_mpa";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Mpa(
                rs.getInt("id"),
                rs.getString("name"))
        );
    }

    public Mpa getMpaById(Integer mpaId) {
        if (mpaId == null) {
            throw new ValidationException("Передан пустой аргумент!");
        }
        Mpa mpa;
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM ratings_mpa WHERE id = ?", mpaId);
        if (mpaRows.first()) {
            mpa = new Mpa(
                    mpaRows.getInt("id"),
                    mpaRows.getString("name")
            );
        } else {
            throw new MpaNotFoundException("Рейтинг с ID=" + mpaId + " не найден!");
        }
        return mpa;
    }
}