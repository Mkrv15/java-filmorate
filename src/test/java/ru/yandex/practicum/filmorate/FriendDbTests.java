package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FriendDbStorage.class, UserDbStorage.class})
class FriendDbTests {

    @Autowired
    private FriendDbStorage friendStorage;

    @Autowired
    private UserDbStorage userStorage;

    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() {
        User user1 = new User(null, "user1@example.com", "user1", "User One",
                LocalDate.of(1990, 1, 1), null);
        User created1 = userStorage.create(user1);
        userId1 = created1.getId();

        User user2 = new User(null, "user2@example.com", "user2", "User Two",
                LocalDate.of(1991, 2, 2), null);
        User created2 = userStorage.create(user2);
        userId2 = created2.getId();
    }

    @Test
    public void testAddFriend() {
        friendStorage.addFriend(userId1, userId2);

        List<User> friends = friendStorage.getFriends(userId1);

        assertThat(friends).isNotEmpty();
        assertThat(friends.get(0).getId()).isEqualTo(userId2);
    }

    @Test
    public void testDeleteFriend() {
        friendStorage.addFriend(userId1, userId2);
        friendStorage.deleteFriend(userId1, userId2);

        List<User> friends = friendStorage.getFriends(userId1);

        assertThat(friends).isEmpty();
    }

    @Test
    public void testGetFriends() {
        friendStorage.addFriend(userId1, userId2);

        List<User> friends = friendStorage.getFriends(userId1);

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getEmail()).isEqualTo("user2@example.com");
    }
}
