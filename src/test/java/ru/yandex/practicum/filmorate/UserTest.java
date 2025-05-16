package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    public void canCreateTrueUser() {
        User user = User.builder()
                .email("user@yandex.ru")
                .login("login_user")
                .name("first name")
                .birthday(LocalDate.of(2000, 3, 15))
                .build();

        Set<String> violations = validator.validate(user)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violations.isEmpty(), "Ожидается отсутствие нарушений валидации");
    }

    @Test
    public void ifEmailIsEmpty() {
        User user = User.builder()
                .login("login_user")
                .name("first name")
                .birthday(LocalDate.of(2000, 3, 15))
                .build();

        Set<String> violations = validator.validate(user)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violations.contains("Email не может быть пустым"));
    }

    @Test
    public void ifEmailIsWrong() {
        User user = User.builder()
                .email("qwerty")
                .login("login_user")
                .name("first name")
                .birthday(LocalDate.of(2000, 3, 15))
                .build();

        Set<String> violations = validator.validate(user)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violations.contains("Неверный email"));
    }

    @Test
    public void ifLoginIsEmpty() {
        User user = User.builder()
                .email("user@yandex.ru")
                .name("first name")
                .birthday(LocalDate.of(2000, 3, 15))
                .build();

        Set<String> violations = validator.validate(user)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertTrue(violations.contains("Login не может быть пустым"));
    }

    @Test
    public void ifLoginContainsSpace() {
        User user = User.builder()
                .email("user@yandex.ru")
                .login("login user ")
                .name("first name")
                .birthday(LocalDate.of(2000, 3, 15))
                .build();

        Set<String> violations = validator.validate(user)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violations.contains("Login не может содержать пробелы"));
    }

    @Test
    public void ifDateIsInFuture() {
        User user = User.builder()
                .email("user@yandex.ru")
                .login("login_user")
                .name("first name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Set<String> violations = validator.validate(user)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertFalse(violations.isEmpty(), "Ожидается ообщение об ошибке");
    }

    @Test
    public void ifDateIsToday() {
        User user = User.builder()
                .email("user@yandex.ru")
                .login("login_user")
                .name("first name")
                .birthday(LocalDate.now())
                .build();

        Set<String> violations = validator.validate(user)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violations.isEmpty(), "Ожидается отсутствие ошибки");
    }

    @Test
    public void ifDateIsInPast() {
        User user = User.builder()
                .email("user@yandex.ru")
                .login("login_user")
                .name("first name")
                .birthday(LocalDate.now().minusDays(1))
                .build();

        Set<String> violations = validator.validate(user)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violations.isEmpty(), "Ожидается отсутствие ошибки");
    }
}
