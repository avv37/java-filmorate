package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    @Qualifier("FilmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("UserDbStorage")
    private final UserStorage userStorage;

    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        log.info("Отдали коллекцию, количество фильмов: {}", films.size());
        return films;
    }

    public Film getFilmById(Integer filmId) {
        log.info("getFilmById = {}", filmId);
        Optional<Film> filmOpt = filmStorage.getFilmById(filmId);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        return filmOpt.get();
    }

    public Film addFilm(Film film) {
        log.info("создание нового фильма");
        checkFilmOrThrow(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("изменение существующего фильма");
        checkFilmOrThrow(film);
        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        return filmStorage.updateFilm(film);
    }

    public void like(Integer filmId, Integer userId) {
        log.info("фильму {} ставит лайк юзер {}", filmId, userId);
        Optional<User> userOpt = userStorage.getUserById(userId);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        Optional<Film> filmOpt = filmStorage.getFilmById(filmId);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        filmStorage.like(filmId, userId);
    }

    public void unLike(Integer filmId, Integer userId) {
        log.info("фильму {} делает анлайк юзер {}", filmId, userId);
        Optional<User> user = userStorage.getUserById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        filmStorage.unLike(filmId, userId);
    }

    public Collection<Film> getMostPopulars(Integer amount) {
        log.info("получение {} популярных фильмов", amount);
        if (amount <= 0) {
            throw new ValidationException("Количество фильмов должно быть больше 0");
        }
        return filmStorage.getMostPopulars(amount);
    }

    public void checkFilmOrThrow(Film film) {
        if (film.getReleaseDate().isBefore(firstFilmDate)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28.12.1895");
        }
        if (film.getMpa() != null) {
            int mpaId = film.getMpa().getId();
            Optional<Mpa> mpaOpt = mpaStorage.getMpaById(mpaId);
            if (mpaOpt.isEmpty()) {
                throw new NotFoundException("Рейтинг с id = " + mpaId + " не найден");
            }
        }
        if (film.getGenres() != null) {
            Set<Genre> genreSet = film.getGenres();
            for (Genre genre : genreSet) {
                int genreId = genre.getId();
                Optional<Genre> genreOpt = genreStorage.getGenreById(genreId);
                if (genreOpt.isEmpty()) {
                    throw new NotFoundException("Жанр с id = " + genreId + " не найден");
                }
            }
        }
    }

}
