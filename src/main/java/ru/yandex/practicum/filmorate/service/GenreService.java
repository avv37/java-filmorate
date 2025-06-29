package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public Collection<Genre> findAll() {
        Collection<Genre> genres = genreStorage.getAllGenres();
        log.info("Отдали коллекцию жанров");
        return genres;
    }

    public Genre getGenreById(Integer genreId) {
        Optional<Genre> genreOpt = genreStorage.getGenreById(genreId);
        if (genreOpt.isEmpty()) {
            throw new NotFoundException("Жанр с id = " + genreId + " не найден");
        }
        return genreOpt.get();
    }
}
