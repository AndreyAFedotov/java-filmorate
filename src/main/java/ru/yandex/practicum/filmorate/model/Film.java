package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class Film {
    private long id;

    @NotBlank
    @NotNull
    private String name;

    @Size(max = 200, message = "Максимальная длина описания - 200 символов")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @Positive
    private int duration;

    private Set<Long> likes;

    private int likesCount;
}
