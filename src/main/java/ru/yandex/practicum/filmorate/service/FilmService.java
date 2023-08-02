package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {
    private static final LocalDate FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film createFilm(Film film) {
        checkReleaseDate(film.getReleaseDate());
        return filmStorage.createFilm(film);
    }

    public Film updateFIlm(Film film) {
        checkFilmIsExist(film.getId());
        checkReleaseDate(film.getReleaseDate());
        return filmStorage.updateFilm(film);
    }

    public Film setLikeToFilm(long id, long userId) {
        checkFilmIsExist(id);
        checkUserIsExist(userId);
        return filmStorage.setLikeToFilm(id, userId);
    }

    public Film deleteLikeFromFilm(long id, long userId) {
        checkFilmIsExist(id);
        checkUserIsExist(userId);
        return filmStorage.deleteLikeFromFilm(id, userId);
    }

    public void checkFilmIsExist(long id) {
        if (!filmStorage.isExists(id)) {
            throw new NotFoundException("Такого фильма нет в базе: " + id);
        }
    }

    public void checkUserIsExist(long id) {
        if (!userStorage.isExists(id)) {
            throw new NotFoundException("Пользователя не существует: " + id);
        }
    }

    public void checkReleaseDate(LocalDate date) {
        if (date.isBefore(FILM_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
        }
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        if (!filmStorage.getFilms().isEmpty()) {
            return filmStorage.getPopularFilms(count, genreId, year);
        } else {
            throw new NotFoundException("Список фильмов пуст");
        }
    }

    public Film getFilm(long id) {
        if (!filmStorage.isExists(id)) {
            throw new NotFoundException("Фильма не существует: " + id);
        } else {
            return filmStorage.getFilm(id);
        }
    }

    public List<Film> getDirectorsFilms(long directorId, Set<String> sortBy) {
        if (!directorStorage.isExists(directorId)) {
            throw new NotFoundException("Режиссёра не существует: " + directorId);
        } else {
            return filmStorage.getDirectorsFilms(directorId, sortBy);
        }
    }
}
