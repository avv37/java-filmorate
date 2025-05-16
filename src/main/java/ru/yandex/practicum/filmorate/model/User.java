package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    private Integer id;
    @Email(message = "Неверный email")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    @NotBlank(message = "Login не может быть пустым")
    private String login;
    @EqualsAndHashCode.Exclude
    private String name;
    @PastOrPresent
    private LocalDate birthday;
}
