package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFIlm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film setLikeToFilm(@PathVariable long id,
                              @PathVariable long userId) {
        return filmService.setLikeToFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLikeFromFilm(@PathVariable long id,
                                   @PathVariable long userId) {
        return filmService.deleteLikeFromFilm(id, userId);
    }

    @DeleteMapping("/{filmId}")
    public Film deleteFilm(@PathVariable long filmId) {
        return filmService.deleteFilm(filmId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(value = "count", defaultValue = "10") Integer count,
                                      @RequestParam(defaultValue = "0") Integer genreId,
                                      @RequestParam(defaultValue = "0") Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable long id) {
        return filmService.getFilm(id);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getDirectorsFilms(@PathVariable long directorId,
                                        @RequestParam Set<String> sortBy) {
        return filmService.getDirectorsFilms(directorId, sortBy);
    }
}
