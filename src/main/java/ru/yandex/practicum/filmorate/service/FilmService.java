package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        log.info("Отдали коллекцию, количество фильмов: {}", films.size());
        return films;
    }

    public Film getFilmById(Integer filmId) {
        Optional<Film> filmOpt = filmStorage.getFilmById(filmId);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        return filmOpt.get();
    }

    public Film addFilm(Film film) {
        log.info("создание нового фильма");
        if (film.getReleaseDate().isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28.12.1895");
        }
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        log.info("изменение существующего фильма");
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (filmStorage.getFilmById(newFilm.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28.12.1895");
        }
        return filmStorage.updateFilm(newFilm);
    }

    public Film like(Integer filmId, Integer userId) {
        log.info("фильму {} ставит лайк юзер {}", filmId, userId);
        Optional<User> user = userStorage.getUserById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        return filmStorage.like(filmId, userId);
    }

    public Film unLike(Integer filmId, Integer userId) {
        log.info("фильму {} делает анлайк юзер {}", filmId, userId);
        Optional<User> user = userStorage.getUserById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        return filmStorage.unLike(filmId, userId);
    }

    public Collection<Film> getMostPopulars(Integer amount) {
        log.info("получение {} популярных фильмов", amount);
        if (amount <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше 0");
        }
        return filmStorage.getMostPopulars(amount);
    }

}
