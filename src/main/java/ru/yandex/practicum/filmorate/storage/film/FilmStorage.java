package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    List<Film> getFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    boolean isExists(long id);

    Film setLikeToFilm(long id, long userId);

    Film deleteLikeFromFilm(long id, long userId);

    List<Film> getPopularFilms(Integer count);

    Film getFilm(long id);

    List<Film> getDirectorsFilms(long directorId, Set<String> sortBy);


    Film deleteFilm(long id);
}
