package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    @Email(message = "Неверный email")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
    @NotBlank(message = "Login не может быть пустым")
    @Pattern(regexp = "^\\S*$", message = "Login не может содержать пробелы")
    private String login;
    private String name;
    @PastOrPresent
    @NotNull
    private LocalDate birthday;
    private Set<Integer> friends;
}
