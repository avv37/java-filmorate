package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class})
public class FilmDbStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorage filmDbStorage;
    private final FilmRowMapper filmRowMapper;

    @BeforeEach
    public void beforeEach() {
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        String query = "INSERT INTO films (name, description, release_date, duration, mpa_id)  VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                query,
                "film1", "film1 is good", LocalDate.of(1991, 1, 1), 100, 1
        );
        jdbcTemplate.update(
                query,
                "film2", "film2 is good", LocalDate.of(1992, 2, 2), 200, 2
        );
    }

    @Test
    public void testGetFilmById() {
        Optional<Film> filmOpt = filmDbStorage.getFilmById(1);
        assertTrue(filmOpt.isPresent());
        Film film = filmOpt.get();
        assertThat(film).isNotNull()
                .hasFieldOrPropertyWithValue("name", "film1")
                .hasFieldOrPropertyWithValue("description", "film1 is good")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1991, 1, 1))
                .hasFieldOrPropertyWithValue("duration", 100)
                .hasFieldOrPropertyWithValue("mpa", new Mpa(1, "G"));
    }

    @Test
    public void testAddFilm() {
        Film film3 = Film.builder()
                .name("film3")
                .description("film3 is good")
                .releaseDate(LocalDate.of(1993, 3, 3))
                .duration(300)
                .mpa(new Mpa(3, "PG-13"))
                .genres(Set.of(Genre.builder().id(1).build(), Genre.builder().id(2).build()))
                .build();
        filmDbStorage.addFilm(film3);

        Optional<Film> filmOpt = filmDbStorage.getFilmById(3);
        assertTrue(filmOpt.isPresent());

        Film film = filmOpt.get();
        assertThat(film).isNotNull()
                .hasFieldOrPropertyWithValue("name", "film3")
                .hasFieldOrPropertyWithValue("description", "film3 is good")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1993, 3, 3))
                .hasFieldOrPropertyWithValue("duration", 300)
                .hasFieldOrPropertyWithValue("mpa", new Mpa(3, "PG-13"))
                .hasFieldOrPropertyWithValue("genres", Set.of(
                        Genre.builder().id(1).name("Комедия").build(),
                        Genre.builder().id(2).name("Драма").build()
                ));
    }

    @Test
    public void testGetAllFilms() {
        List<Film> films = filmDbStorage.findAll();
        assertEquals(2, films.size());

        Film film = films.get(0);
        assertThat(film).isNotNull()
                .hasFieldOrPropertyWithValue("name", "film1")
                .hasFieldOrPropertyWithValue("description", "film1 is good")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1991, 1, 1))
                .hasFieldOrPropertyWithValue("duration", 100)
                .hasFieldOrPropertyWithValue("mpa", new Mpa(1, "G"));

        film = films.get(1);
        assertThat(film).isNotNull()
                .hasFieldOrPropertyWithValue("name", "film2")
                .hasFieldOrPropertyWithValue("description", "film2 is good")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1992, 2, 2))
                .hasFieldOrPropertyWithValue("duration", 200)
                .hasFieldOrPropertyWithValue("mpa", new Mpa(2, "PG"));

    }

    @Test
    public void testUpdateFilm() {
        Film film = Film.builder()
                .id(2)
                .name("film222")
                .description("film222 is good")
                .releaseDate(LocalDate.of(1992, 12, 12))
                .duration(222)
                .mpa(new Mpa(3, "PG-13"))
                .genres(Set.of(Genre.builder().id(1).build(), Genre.builder().id(2).build()))
                .build();
        filmDbStorage.updateFilm(film);

        String query = "SELECT * FROM films WHERE id = ?";
        Film newFilm = jdbcTemplate.queryForObject(query, filmRowMapper, 2);
        assertThat(newFilm).isNotNull()
                .hasFieldOrPropertyWithValue("name", "film222")
                .hasFieldOrPropertyWithValue("description", "film222 is good")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1992, 12, 12))
                .hasFieldOrPropertyWithValue("duration", 222)
                .hasFieldOrPropertyWithValue("mpa", new Mpa(3, null));

        Optional<Film> filmOpt = filmDbStorage.getFilmById(2);
        assertTrue(filmOpt.isPresent());
        newFilm = filmOpt.get();
        assertThat(newFilm).isNotNull()
                .hasFieldOrPropertyWithValue("name", "film222")
                .hasFieldOrPropertyWithValue("description", "film222 is good")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1992, 12, 12))
                .hasFieldOrPropertyWithValue("duration", 222)
                .hasFieldOrPropertyWithValue("mpa", new Mpa(3, "PG-13"))
                .hasFieldOrPropertyWithValue("genres", Set.of(
                        Genre.builder().id(1).name("Комедия").build(),
                        Genre.builder().id(2).name("Драма").build()
                ));
    }

    @Test
    public void testDeleteFilm() {
        filmDbStorage.deleteFilm(1);
        ;
        String query = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(query, filmRowMapper, 1);
        assertTrue(films.isEmpty());
    }

    @Test
    public void testLike() {
        addUsers1to4();
        filmDbStorage.like(1, 1);
        filmDbStorage.like(1, 2);

        Optional<Film> filmOpt = filmDbStorage.getFilmById(1);
        assertTrue(filmOpt.isPresent());

        Film film = filmOpt.get();
        assertEquals(new HashSet<>(Set.of(1, 2)), film.getLikes());
    }

    @Test
    public void testUnLike() {
        addUsers1to4();
        filmDbStorage.like(1, 1);
        filmDbStorage.like(1, 2);
        filmDbStorage.unLike(1, 1);

        Optional<Film> filmOpt = filmDbStorage.getFilmById(1);
        assertTrue(filmOpt.isPresent());

        Film film = filmOpt.get();
        assertEquals(new HashSet<>(Set.of(2)), film.getLikes());
    }

    @Test
    public void testGetMostPopular() {
        addUsers1to4();
        Film film = Film.builder()
                .name("film3")
                .description("film3 is good")
                .releaseDate(LocalDate.of(1993, 3, 3))
                .duration(300)
                .mpa(new Mpa(3, "PG-13"))
                .genres(Set.of(Genre.builder().id(1).build(), Genre.builder().id(2).build()))
                .build();
        filmDbStorage.addFilm(film);
        film = Film.builder()
                .name("film4")
                .description("film4 is good")
                .releaseDate(LocalDate.of(1994, 4, 4))
                .duration(400)
                .mpa(new Mpa(3, "PG-13"))
                .genres(Set.of(Genre.builder().id(1).build(), Genre.builder().id(2).build()))
                .build();
        filmDbStorage.addFilm(film);
        film = Film.builder()
                .name("film5")
                .description("film5 is good")
                .releaseDate(LocalDate.of(1995, 5, 5))
                .duration(500)
                .mpa(new Mpa(2, "PG"))
                .genres(Set.of(Genre.builder().id(1).build(), Genre.builder().id(2).build()))
                .build();
        filmDbStorage.addFilm(film);

        filmDbStorage.like(1, 1);

        filmDbStorage.like(2, 1);
        filmDbStorage.like(2, 2);
        filmDbStorage.like(2, 3);

        filmDbStorage.like(3, 1);
        filmDbStorage.like(3, 2);
        filmDbStorage.like(3, 3);

        filmDbStorage.like(4, 3);
        filmDbStorage.like(4, 4);

        filmDbStorage.like(5, 4);

        List<Film> films = filmDbStorage.getMostPopulars(3);

        assertEquals(3, films.size());

        film = films.get(0);
        assertThat(film).isNotNull()
                .hasFieldOrPropertyWithValue("id", 2);
        film = films.get(1);
        assertThat(film).isNotNull()
                .hasFieldOrPropertyWithValue("id", 3);
        film = films.get(2);
        assertThat(film).isNotNull()
                .hasFieldOrPropertyWithValue("id", 4);

    }

    private void addUsers1to4() {
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
