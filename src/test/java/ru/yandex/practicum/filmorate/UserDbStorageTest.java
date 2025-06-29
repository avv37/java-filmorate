package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        String query = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(
                query,
                "user1@yandex.ru", "login1", "name1",
                LocalDate.of(1991, 1, 1)
        );
        jdbcTemplate.update(
                query,
                "user2@yandex.ru", "login2", "name2",
                LocalDate.of(1992, 2, 2)
        );
    }


    @Test
    public void testGetUserById() {
        Optional<User> userOptional = userDbStorage.getUserById(1);
        assertTrue(userOptional.isPresent());
        User user = userOptional.get();
        assertThat(user).isNotNull()
                .hasFieldOrPropertyWithValue("email", "user1@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "login1")
                .hasFieldOrPropertyWithValue("name", "name1")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1991, 1, 1));
    }

    @Test
    public void testAddUser() {
        User user3 = User.builder()
                .email("user3@yandex.ru")
                .login("login3")
                .name("name3")
                .birthday(LocalDate.of(1993, 3, 3))
                .build();
        userDbStorage.addUser(user3);

        Optional<User> userOptional = userDbStorage.getUserById(3);
        assertTrue(userOptional.isPresent());

        User user = userOptional.get();
        assertThat(user).isNotNull()
                .hasFieldOrPropertyWithValue("email", "user3@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "login3")
                .hasFieldOrPropertyWithValue("name", "name3")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1993, 3, 3));
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = userDbStorage.findAll();
        assertEquals(2, users.size());

        User user = users.get(0);
        assertThat(user).isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("email", "user1@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "login1")
                .hasFieldOrPropertyWithValue("name", "name1")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1991, 1, 1));

        user = users.get(1);
        assertThat(user).isNotNull()
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("email", "user2@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "login2")
                .hasFieldOrPropertyWithValue("name", "name2")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1992, 2, 2));
    }

    @Test
    public void testUpdateUser() {
        User user = User.builder()
                .id(2)
                .email("user22@yandex.ru")
                .login("login22")
                .name("name22")
                .birthday(LocalDate.of(1982, 12, 12))
                .build();
        userDbStorage.updateUser(user);

        String query = "SELECT * FROM users WHERE id = ?";
        User newUser = jdbcTemplate.queryForObject(query, userRowMapper, 2);
        assertThat(newUser).isNotNull()
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("email", "user22@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "login22")
                .hasFieldOrPropertyWithValue("name", "name22")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1982, 12, 12));
    }

    @Test
    public void testDeleteUser() {
        userDbStorage.deleteUser(1);
        String query = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(query, userRowMapper, 1);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testAddFriend() {
        addUsers3and4();
        userDbStorage.addFriend(1, 3);
        userDbStorage.addFriend(1, 4);

        Optional<User> userOpt = userDbStorage.getUserById(1);
        User user = userOpt.get();
        assertEquals(1, user.getId());
        assertEquals(new HashSet<>(Set.of(3, 4)), user.getFriends());
    }

    @Test
    public void testGetFriends() {
        addUsers3and4();
        userDbStorage.addFriend(1, 3);
        userDbStorage.addFriend(1, 4);

        List<User> friends = userDbStorage.getFriends(1);
        HashSet<Integer> friendsId = (HashSet<Integer>) friends.stream()
                .map(user1 -> user1.getId())
                .collect(Collectors.toSet());
        assertEquals(new HashSet<>(Set.of(3, 4)), friendsId);
    }

    @Test
    public void testGetCommonFriends() {
        addUsers3and4();
        userDbStorage.addFriend(1, 3);
        userDbStorage.addFriend(1, 4);

        userDbStorage.addFriend(2, 1);
        userDbStorage.addFriend(2, 3);

        List<User> friends = userDbStorage.getCommonFriends(1, 2);
        HashSet<Integer> friendsId = (HashSet<Integer>) friends.stream()
                .map(user1 -> user1.getId())
                .collect(Collectors.toSet());

        assertEquals(new HashSet<>(Set.of(3)), friendsId);
    }

    @Test
    public void testDeleteFriend() {
        addUsers3and4();
        userDbStorage.addFriend(1, 3);
        userDbStorage.addFriend(1, 4);

        userDbStorage.deleteFriend(1, 4);

        Optional<User> userOpt = userDbStorage.getUserById(1);
        User user = userOpt.get();
        assertEquals(1, user.getId());
        assertEquals(new HashSet<>(Set.of(3)), user.getFriends());
    }

    private void addUsers3and4() {
        String query = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(
                query,
                "user3@yandex.ru", "login3", "name3",
                LocalDate.of(1993, 3, 3)
        );
        jdbcTemplate.update(
                query,
                "user4@yandex.ru", "login4", "name4",
                LocalDate.of(1994, 4, 4)
        );
    }

}
