package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage{
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private final static String INSERT_QUERY = "INSERT INTO users (username, login, email, birthday)" +
            " VALUES (?, ?, ?, ?)";
    private final static String UPDATE_QUERY = "UPDATE users SET username = ?, login = ?, email = ?, birthday = ? " +
            "WHERE id = ?";

    @Override
    public User create(User user) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(id);

        log.info("В БД добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User delete(Long id) {
        User user = findById(id);

        String deleteFriendsSql = "DELETE FROM friends WHERE to_user_id = ? OR from_user_id = ?";
        jdbcTemplate.update(deleteFriendsSql, id, id);

        String deleteLikesSql = "DELETE FROM likes WHERE user_id = ?";
        jdbcTemplate.update(deleteLikesSql, id);

        String deleteUserSql = "DELETE FROM users WHERE id = ?";
        int deleted = jdbcTemplate.update(deleteUserSql, id);

        if (deleted == 0) {
            log.error("Пользователь с id = {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }

        log.info("Пользователь {} удален из БД", user);
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId()==null) {
            throw new ValidationException();
        }

        int updated = jdbcTemplate.update(UPDATE_QUERY,
                newUser.getName(),
                newUser.getLogin(),
                newUser.getEmail(),
                Date.valueOf(newUser.getBirthday()),
                newUser.getId()
        );

        if (updated == 0) {
            log.error("Пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        log.info("Пользователь {} обновлен в БД", newUser);
        return findById(newUser.getId());
    }

    @Override
    public List<User> findAll() {
        List<User> users = jdbcTemplate.query(FIND_ALL_QUERY, userMapper);

        users.forEach(this::loadUserFriends);

        return users;
    }

    @Override
    public User findById(long id) {
        try {
            User user = jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, userMapper, id);
            loadUserFriends(user);
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.error("Пользователь с id = {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    private void loadUserFriends(User user) {
        String sql = "SELECT to_user_id FROM friends WHERE from_user_id = ?";
        List<Long> friends = jdbcTemplate.queryForList(sql, Long.class, user.getId());
        user.getFriends().clear();
        user.getFriends().addAll(friends);
    }
}
