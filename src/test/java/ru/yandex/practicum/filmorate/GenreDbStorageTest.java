package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class})
public class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;

    @Test
    public void testGetGenreById() {
        Optional<Genre> genreOptional = genreDbStorage.getGenreById(5);
        assertTrue(genreOptional.isPresent());

        Genre genre = genreOptional.get();
        assertThat(genre).isNotNull()
                .hasFieldOrPropertyWithValue("id", 5)
                .hasFieldOrPropertyWithValue("name", "Документальный");

        assertTrue(genreDbStorage.getGenreById(7).isEmpty());
    }

    @Test
    public void getAllGenres() {
        List<Genre> genres = genreDbStorage.getAllGenres();
        Genre genre = genres.get(0);
        assertThat(genre).isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
        genre = genres.get(1);
        assertThat(genre).isNotNull()
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "Драма");
        genre = genres.get(2);
        assertThat(genre).isNotNull()
                .hasFieldOrPropertyWithValue("id", 3)
                .hasFieldOrPropertyWithValue("name", "Мультфильм");
        genre = genres.get(3);
        assertThat(genre).isNotNull()
                .hasFieldOrPropertyWithValue("id", 4)
                .hasFieldOrPropertyWithValue("name", "Триллер");
        genre = genres.get(4);
        assertThat(genre).isNotNull()
                .hasFieldOrPropertyWithValue("id", 5)
                .hasFieldOrPropertyWithValue("name", "Документальный");
        genre = genres.get(5);
        assertThat(genre).isNotNull()
                .hasFieldOrPropertyWithValue("id", 6)
                .hasFieldOrPropertyWithValue("name", "Боевик");

    }
}
