package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    private long id;

    @NotBlank
    @NotNull
    @Email
    private String email;

    @NotBlank
    @NotNull
    @Pattern(regexp = "\\S+") // "\\S+"(не пробельные) либо "[^\s]+"(кроме пробела)
    private String login;

    private String name;

    @PastOrPresent
    private LocalDate birthday;

    private Set<Long> friends;
}
