package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        Film oldFilm = films.get(film.getId());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setName(film.getName());
        oldFilm.setDuration(film.getDuration());
        oldFilm.setReleaseDate(film.getReleaseDate());
        return oldFilm;
    }

    @Override
    public void deleteFilm(Integer id) {
        films.remove(id);
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film like(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
        film.getLikes().add(userId);
        return film;
    }

    @Override
    public Film unLike(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
        film.getLikes().remove(userId);
        return film;
    }

    @Override
    public Collection<Film> getMostPopulars(Integer amount) {
        Comparator<Film> comparator = (o1, o2) -> Integer.compare(o2.getLikes().size(), o1.getLikes().size());
        return films.values().stream()
                .sorted(comparator)
                .limit(amount)
                .collect(Collectors.toList());
    }

    private Integer getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
