package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        User oldUser = users.get(user.getId());

        oldUser.setEmail(user.getEmail());
        oldUser.setLogin(user.getLogin());
        if (user.getName() == null || user.getName().isBlank()) {
            oldUser.setName(user.getLogin());
        } else {
            oldUser.setName(user.getName());
        }

        oldUser.setBirthday(user.getBirthday());
        return oldUser;
    }

    @Override
    public void deleteUser(Integer id) {
        users.remove(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User addFriend(Integer id, Integer friendId) {
        User user = users.get(id);
        user.getFriends().add(friendId);
        return user;
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        User user = users.get(id);
        user.getFriends().remove(friendId);
    }

    @Override
    public Collection<User> getFriends(Integer id) {
        User user = users.get(id);
        return user.getFriends().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(Integer id, Integer friendId) {
        User user = users.get(id);
        User friend = users.get(friendId);
        Set<Integer> intersection = user.getFriends();
        intersection.retainAll(friend.getFriends());
        return intersection.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
