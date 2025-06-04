package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class FilmTest {

    private static Validator validator;

    private final FilmService filmService = new FilmService(
            new InMemoryFilmStorage(),
            new InMemoryUserStorage()
    );

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    public void canCreateTrueFilm() {
        Film film = Film.builder()
                .name("Белое солнце пустыни")
                .description("о приключениях красноармейца Фёдора Ивановича Сухова")
                .duration(83)
                .releaseDate(LocalDate.of(1969, 12, 14))
                .build();

        Set<String> violations = validator.validate(film)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violations.isEmpty(), "Ожидается отсутствие нарушений валидации");
    }

    @Test
    public void ifNameIsEmpty() {
        Film film = Film.builder()
                .description("о приключениях красноармейца Фёдора Ивановича Сухова")
                .duration(83)
                .releaseDate(LocalDate.of(1969, 12, 14))
                .build();

        Set<String> violations = validator.validate(film)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violations.contains("Название не может быть пустым"));
    }

    @Test
    public void ifDescriptionMoreThen200() {
        Film film = Film.builder()
                .name("Белое солнце пустыни")
                .description("советский художественный фильм 1970 года в жанре истерн[3] режиссёра Владимира Мотыля, " +
                        "повествующий о приключениях красноармейца Фёдора Ивановича Сухова, " +
                        "спасающего женщин из гарема бандита Абдуллы в г")
                .duration(83)
                .releaseDate(LocalDate.of(1969, 12, 14))
                .build();

        Set<String> violations = validator.validate(film)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violations.contains("Максимальная длина описания — 200 символов"));
    }

    @Test
    public void ifDescriptionEquals200() {
        Film film = Film.builder()
                .name("Белое солнце пустыни")
                .description("советский художественный фильм 1970 года в жанре истерн[3] режиссёра Владимира Мотыля, " +
                        "повествующий о приключениях красноармейца Фёдора Ивановича Сухова, " +
                        "спасающего женщин из гарема бандита Абдуллы в ")
                .duration(83)
                .releaseDate(LocalDate.of(1969, 12, 14))
                .build();

        Set<String> violations = validator.validate(film)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violations.isEmpty(), "Ожидается отсутствие нарушений валидации");
    }

    @Test
    public void ifDurationIsNegative() {
        Film film = Film.builder()
                .name("Белое солнце пустыни")
                .description("о приключениях красноармейца Фёдора Ивановича Сухова")
                .duration(-1)
                .releaseDate(LocalDate.of(1969, 12, 14))
                .build();

        Set<String> violations = validator.validate(film)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertFalse(violations.isEmpty(), "Ожидается ообщение об ошибке");
    }

    @Test
    public void ifDurationIs0() {
        Film film = Film.builder()
                .name("Белое солнце пустыни")
                .description("о приключениях красноармейца Фёдора Ивановича Сухова")
                .duration(0)
                .releaseDate(LocalDate.of(1969, 12, 14))
                .build();

        Set<String> violations = validator.validate(film)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertFalse(violations.isEmpty(), "Ожидается ообщение об ошибке");
    }

    @Test
    public void ifReleaseDateIsBefore() throws Exception {
        Film film = Film.builder()
                .name("Белое солнце пустыни")
                .description("о приключениях красноармейца Фёдора Ивановича Сухова")
                .duration(83)
                .releaseDate(LocalDate.of(1895, 12, 27))
                .build();

        ValidationException validationException = assertThrows(
                ValidationException.class,
                () -> filmService.addFilm(film)
        );

        assertTrue(validationException.getMessage()
                        .contains("Дата релиза должна быть не раньше 28.12.1895"),
                "Ожидается сообщение об ошибке"
        );
    }

}
