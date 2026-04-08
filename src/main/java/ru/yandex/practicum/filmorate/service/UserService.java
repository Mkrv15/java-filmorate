package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public List<User> findAll() {

        return userStorage.findAll();

    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public void addFriend(long userId, long friendId) {
        userStorage.findById(userId);
        userStorage.findById(friendId);

        String sql = "INSERT INTO friends (to_user_id, from_user_id, status_id) VALUES (?, ?, 1)";
        try {
            jdbcTemplate.update(sql, friendId, userId);
            log.info("Пользователь {} добавлен в друзья к {}", friendId, userId);
        } catch (Exception e) {
            log.warn("Дружба уже существует: {}", e.getMessage());
        }
    }

    public void removeFriend(long userId, long friendId) {
        String sql = "DELETE FROM friends WHERE from_user_id = ? AND to_user_id = ?";
        int deleted = jdbcTemplate.update(sql, userId, friendId);

        if (deleted == 0) {
            log.info("Пользователь {} не найден в друзьях у {}", friendId, userId);
        } else {
            log.info("Пользователь {} удален из друзей {}", friendId, userId);
        }
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        String sql = "SELECT u.* FROM users u " +
                "WHERE u.id IN (" +
                "  SELECT f1.to_user_id FROM friends f1 WHERE f1.from_user_id = ?" +
                "  INTERSECT " +
                "  SELECT f2.to_user_id FROM friends f2 WHERE f2.from_user_id = ?" +
                ")";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("username"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId, friendId);
    }

    public List<User> getFriends(long userId) {
        userStorage.findById(userId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.id = f.to_user_id " +
                "WHERE f.from_user_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("username"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId);
    }
}
