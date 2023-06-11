package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> getFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    boolean isExists(long id);

    Film setLikeToFilm(long id, long userId);

    Film deleteLikeFromFilm(long id, long userId);

    List<Film> getPopularFilms(Integer count);

    Film getFilm(long id);
}
