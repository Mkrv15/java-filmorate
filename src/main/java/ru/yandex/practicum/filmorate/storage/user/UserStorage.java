package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User create(User user);

    User delete(Long id);

    User update(User nawUser);

    List<User> findAll();

    User findById(long id);
}
