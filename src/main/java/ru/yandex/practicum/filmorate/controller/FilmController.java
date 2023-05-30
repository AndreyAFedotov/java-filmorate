package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int filmId = 0;

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка даты релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
        }
        film.setId(getNewId());
        films.put(film.getId(), film);
        log.info("Фильм \"{}\" добавлен в базу", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            log.warn("Фильма нет в базе");
            throw new ValidationException("Такого фильма в базе не существует");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка даты релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
        }
        films.put(film.getId(), film);
        log.info("Фильм \"{}\" обновлен.", film.getName());
        return film;
    }

    @ExceptionHandler(ValidationException.class)
    public String handleException(ValidationException exception) {
        return exception.getMessage();
    }

    public int getNewId() {
        return ++filmId;
    }
}
