package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbTests {

    @Autowired
    private final UserDbStorage userStorage;

    @Test
    public void testFindUserByIdNotFound() {
        assertThatThrownBy(() -> userStorage.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    public void testCreateUser() {
        User user = new User(null, "new@example.com", "newuser", "New User",
                LocalDate.of(1995, 5, 5), null);
        User created = userStorage.create(user);

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(created.getId()));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(userFound ->
                        assertThat(userFound).hasFieldOrPropertyWithValue("email", "new@example.com")
                );
    }

    @Test
    public void testUpdateUser() {
        User user = new User(null, "update@example.com", "updateuser", "Old Name",
                LocalDate.of(1992, 3, 3), null);
        User created = userStorage.create(user);

        created.setName("New Name");
        created.setEmail("updated@example.com");

        userStorage.update(created);

        User userFromDb = userStorage.getUserById(created.getId());

        assertThat(userFromDb).hasFieldOrPropertyWithValue("name", "New Name");
        assertThat(userFromDb).hasFieldOrPropertyWithValue("email", "updated@example.com");
    }

    @Test
    public void testDeleteUser() {
        User user = new User(null, "delete@example.com", "deleteuser", "Delete User",
                LocalDate.of(1988, 8, 8), null);
        User created = userStorage.create(user);

        userStorage.delete(created.getId());

        assertThatThrownBy(() -> userStorage.getUserById(created.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void testGetAllUsers() {
        userStorage.create(new User(null, "a@a.com", "usera", "User A",
                LocalDate.of(1990, 1, 1), null));
        userStorage.create(new User(null, "b@b.com", "userb", "User B",
                LocalDate.of(1991, 2, 2), null));

        assertThat(userStorage.getUsers()).isNotEqualTo(2);
    }

    @Test
    public void testGetUserById() {
        User user = new User(null, "test@example.com", "testuser", "Test User",
                LocalDate.of(1990, 1, 1), null);
        User created = userStorage.create(user);

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(created.getId()));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(userFound ->
                        assertThat(userFound).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }
}