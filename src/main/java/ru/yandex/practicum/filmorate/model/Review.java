package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class Review {
    private Long reviewId;

    @NotBlank
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;

    @Builder.Default
    private Long useful = 0L;
}
