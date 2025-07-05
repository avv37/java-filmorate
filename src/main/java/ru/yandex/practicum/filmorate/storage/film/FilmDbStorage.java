package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Repository("FilmDbStorage")
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private final UserRowMapper userRowMapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public Film addFilm(Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }
        if (film.getMpa() == null) {
            film.setMpa(new Mpa());
        }

        String query = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKeyAs(Integer.class));
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }
        film.setMpa(getFilmWithMpa(film));
        return setGenres(film);
    }

    @Override
    public Film updateFilm(Film film) {
        Integer id = film.getId();
        Optional<Film> oldFilmOpt = getFilmById(id);
        if (oldFilmOpt.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        String query = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(
                query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                id
        );
        deleteGenresFromFilm(id);
        return setGenres(film);
    }

    @Override
    public void deleteFilm(Integer id) {
        String query = "DELETE FROM films WHERE id = ?";
        int rowsDeleted = jdbcTemplate.update(query, id);
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name "
                + "FROM FILMS f "
                + "LEFT JOIN mpa m ON f.mpa_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        for (Film film : films) {
            Integer id = film.getId();
            Set<Genre> genres = getGenresByFilmId(id);
            film.setGenres(genres);
            film.setMpa(getFilmWithMpa(film));
            film.setLikes(getLikes(id));
        }
        return films;
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        String query = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(query, filmRowMapper, id);
            Set<Genre> genres = getGenresByFilmId(id);
            film.setGenres(genres);
            film.setMpa(getFilmWithMpa(film));
            film.setLikes(getLikes(id));
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void like(Integer filmId, Integer userId) {
        String query = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(query, filmId, userId);
    }

    @Override
    public void unLike(Integer filmId, Integer userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(query, filmId, userId);
    }

    @Override
    public List<Film> getMostPopulars(Integer amount) {
        String query = """
                SELECT films.*,
                COUNT(likes.user_id) AS likes_count
                FROM films
                JOIN mpa ON films.mpa_id = mpa.id
                LEFT JOIN likes ON films.id = likes.film_id
                GROUP BY films.id
                ORDER BY likes_count DESC, films.id
                LIMIT ?
                """;
        List<Film> films = jdbcTemplate.query(query, new FilmRowMapper(), amount);
        for (Film film : films) {
            Integer id = film.getId();
            Set<Genre> genres = getGenresByFilmId(id);
            film.setGenres(genres);
            film.setMpa(getFilmWithMpa(film));
            film.setLikes(getLikes(id));
        }
        return films;
    }

    private Set<Genre> getGenresByFilmId(Integer id) {
        String query = "SELECT fg.genre_id AS id, g.name AS name " +
                "FROM film_genres AS fg " +
                "JOIN genre AS g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? ORDER BY g.id";
        List<Genre> genreList = jdbcTemplate.query(query, new GenreRowMapper(), id);
        return new HashSet<>(genreList);
    }

    private Film setGenres(Film film) {
        Set<Genre> filmGenreSet = film.getGenres();
        if (filmGenreSet != null) {
            GenreStorage genreStorage = new GenreDbStorage(jdbcTemplate);
            List<Genre> allGenres = genreStorage.getAllGenres();
            Set<Integer> allGenresId = allGenres.stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            for (Genre genre : filmGenreSet) {
                Integer genreId = genre.getId();
                if (allGenresId.contains(genreId)) {
                    setGenreToFilm(film.getId(), genreId);
                }
            }
        }
        return film;
    }

    private Mpa getFilmWithMpa(Film film) {
        Mpa mpa = film.getMpa();
        if (mpa.getId() != null) {
            MpaStorage mpaStorage = new MpaDbStorage(jdbcTemplate);
            Optional<Mpa> mpaOpt = mpaStorage.getMpaById(mpa.getId());
            if (mpaOpt.isPresent()) {
                mpa = mpaOpt.get();
            }
        }
        return mpa;
    }

    private void setGenreToFilm(Integer filmId, Integer genreId) {
        String query = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.update(query, filmId, genreId);
    }

    private void deleteGenresFromFilm(Integer filmId) {
        String query = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(query, filmId);
    }

    private Set<Integer> getLikes(Integer id) {
        String query = "SELECT u.* FROM users u JOIN likes l ON u.id = l.user_id WHERE l.film_id = ?";
        List<User> users = jdbcTemplate.query(query, userRowMapper, id);
        return users.stream()
                .map(user -> user.getId())
                .collect(Collectors.toSet());
    }

}
