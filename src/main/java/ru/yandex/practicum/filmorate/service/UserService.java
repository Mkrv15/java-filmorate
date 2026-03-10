package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        if (!user.getFriends().contains(friendId)) {
            user.getFriends().add(friendId);
            friend.getFriends().add(userId);
            log.info("Пользователь {} добавлен в друзья к {}", friend, user);
        }
        log.info("Пользователь {} был в друзьях у {}", friend, user);
    }

    public void removeFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        if (user.getFriends().contains(friendId)) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
            log.info("Пользователь {} удален из друзей {}", friend, user);
            return;
        }
        log.info("Пользователь {} не найден в друзьях у {}", friend, user);
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        return user.getFriends()
                .stream()
                .filter(user1 -> friend.getFriends().contains(user1))
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public List<User> getFriends(long userId) {
        User user = userStorage.findById(userId);

        return user.getFriends()
                .stream()
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}
