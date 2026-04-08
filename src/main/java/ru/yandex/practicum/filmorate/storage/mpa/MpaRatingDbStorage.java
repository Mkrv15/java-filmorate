package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.MPA;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaRatingDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<MPA> getAllMpa(){
        String sql = "SELECT id, name FROM mpa ORDER BY id";

        return jdbcTemplate.query(sql,(rs, rowNum) -> {
            String mpaName = rs.getString("name");
            return MPA.valueOf(mpaName);
        });
    }

    public MPA getMpaById(int id){
        String sql = "SELECT name FROM mpa WHERE id = ?";
        try{
            String mpaName = jdbcTemplate.queryForObject(sql,String.class,id);
            return MPA.valueOf(mpaName);
        } catch (EmptyResultDataAccessException e){
            log.error("MPA с id {} не найден", id);
            throw new NotFoundException("MPA с id " + id + " не найден");
        }
    }

    public Integer getMpaId(MPA mpa){
        if (mpa == null){
            return null;
        }
        String sql = "SELECT id FROM mpa WHERE name = ?";
        try {
            return jdbcTemplate.queryForObject(sql,Integer.class,mpa.name());
        }catch (EmptyResultDataAccessException e){
            log.error("MPA {} не найден в БД", mpa.name());
            return null;
        }
    }
}
