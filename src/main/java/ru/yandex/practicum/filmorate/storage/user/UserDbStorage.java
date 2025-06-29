package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("UserDbStorage")
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String query = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKeyAs(Integer.class));
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }

        return user;
    }

    @Override
    public User updateUser(User user) {
        Integer id = user.getId();
        String query = "UPDATE USERS SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(
                query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                id
        );
        return user;
    }

    @Override
    public void deleteUser(Integer id) {
        String query = "DELETE FROM users WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(query, id);
    }

    @Override
    public List<User> findAll() {
        String query = "SELECT * FROM users";
        return jdbcTemplate.query(query, userRowMapper);
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(query, userRowMapper, id);

            List<User> friends = getFriends(id);
            HashSet<Integer> friendsId = (HashSet<Integer>) friends.stream()
                    .map(user1 -> user1.getId())
                    .collect(Collectors.toSet());
            user.setFriends(friendsId);

            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public User addFriend(Integer id, Integer friendId) {
        String query = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(query, id, friendId, 1);

        Optional<User> user = getUserById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return user.get();
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        String query = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        int rowsDeleted = jdbcTemplate.update(query, id, friendId);
    }

    @Override
    public List<User> getFriends(Integer id) {
        String query = """
                SELECT u.* FROM users u 
                JOIN friendship f ON u.id = f.friend_id 
                WHERE f.user_id = ?
                """;
        return jdbcTemplate.query(query, userRowMapper, id);
    }

    @Override
    public List<User> getCommonFriends(Integer id, Integer friendId) {
        String query = """
                SELECT u.*  FROM users u
                JOIN friendship f1 ON u.id = f1.friend_id AND f1.user_id = ?
                JOIN friendship f2 ON u.id = f2.friend_id AND f2.user_id = ?
                """;
        return jdbcTemplate.query(query, userRowMapper, id, friendId);
    }


}
