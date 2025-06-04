package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        Collection<User> users = userStorage.findAll();
        log.info("Отдали коллекцию, количество пользователей: {}", users.size());
        return users;
    }

    public User addUser(User user) {
        log.info("создание нового юзера");
        return userStorage.addUser(user);
    }

    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            log.error("Не задан id");
            throw new ValidationException("Id должен быть указан");
        }
        getUserOrThrow(newUser.getId());
        return userStorage.updateUser(newUser);
    }

    public User addFriend(Integer id, Integer friendId) {
        log.info("юзеру {} добавляем френда {}", id, friendId);
        getUserOrThrow(id);
        getUserOrThrow(friendId);
        User user = userStorage.addFriend(id, friendId);
        userStorage.addFriend(friendId, id);
        return user;
    }

    public void deleteFriend(Integer id, Integer friendId) {
        log.info("у юзера {} удаляем френда {}", id, friendId);
        getUserOrThrow(id);
        getUserOrThrow(friendId);
        userStorage.deleteFriend(id, friendId);
        userStorage.deleteFriend(friendId, id);
    }

    public Collection<User> getFriends(Integer id) {
        log.info("получаем друзей юзера {}", id);
        getUserOrThrow(id);
        return userStorage.getFriends(id);
    }

    public Collection<User> getCommonFriends(Integer id, Integer friendId) {
        log.info("получаем общих друзей юзера {} и френда {}", id, friendId);
        getUserOrThrow(id);
        getUserOrThrow(friendId);
        return userStorage.getCommonFriends(id, friendId);
    }

    public User getUserOrThrow(Integer id) {
        Optional<User> user = userStorage.getUserById(id);
        if (user.isEmpty()) {
            log.info("Пользователь с id = {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return user.get();
    }
}
