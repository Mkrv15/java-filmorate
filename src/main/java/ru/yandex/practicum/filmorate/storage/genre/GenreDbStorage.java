package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.Genre;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getAllGenres(){
        String sql = "SELECT name FROM genre ORDER BY id";

        return jdbcTemplate.query(sql,(rs, rowNum) -> {
            String genreName = rs.getString("name");
            return Genre.valueOf(genreName);
        });
    }
    public Genre getGenreById(int id){
        String sql  = "SELECT name FROM genre WHERE id = ?";
        try{
            String genreName = jdbcTemplate.queryForObject(sql,String.class,id);
            log.debug("Найден жанр с id {}: {}",id,genreName);
            return Genre.valueOf(genreName);
        }catch (EmptyResultDataAccessException e){
            log.error("Жанр с id {} не найден",id);
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
    }

    public Integer getGenreId(Genre genre){
        if(genre==null){
            return null;
        }

        String sql = "SELECT id FROM genre WHERE name = ?";
        try{
            return jdbcTemplate.queryForObject(sql,Integer.class,genre.name());
        }catch (EmptyResultDataAccessException e){
            log.error("Жанр {} не найден в БД", genre.name());
            return null;
        }
    }
}
