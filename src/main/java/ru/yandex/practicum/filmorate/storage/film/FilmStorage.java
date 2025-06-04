package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Integer id);

    Collection<Film> findAll();

    Optional<Film> getFilmById(Integer id);

    Film like(Integer filmId, Integer userId);

    Film unLike(Integer filmId, Integer userId);

    Collection<Film> getMostPopulars(Integer amount);

}
