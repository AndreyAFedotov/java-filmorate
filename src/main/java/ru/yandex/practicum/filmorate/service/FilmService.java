package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {
    private static final LocalDate FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final String ERR_USER = "Пользователя не существует: ";
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;
    private final EventStorage eventStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       DirectorStorage directorStorage,
                       EventStorage eventStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorStorage = directorStorage;
        this.eventStorage = eventStorage;
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
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.ADD, id);
        return filmStorage.setLikeToFilm(id, userId);
    }

    public Film deleteLikeFromFilm(long id, long userId) {
        checkFilmIsExist(id);
        checkUserIsExist(userId);
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, id);
        return filmStorage.deleteLikeFromFilm(id, userId);
    }

    public Film deleteFilm(long id) {
        checkFilmIsExist(id);
        return filmStorage.deleteFilm(id);
    }

    public void checkFilmIsExist(long id) {
        if (!filmStorage.isExists(id)) {
            throw new NotFoundException("Такого фильма нет в базе: " + id);
        }
    }

    public void checkUserIsExist(long id) {
        if (!userStorage.isExists(id)) {
            throw new NotFoundException(ERR_USER + id);
        }
    }

    public void checkReleaseDate(LocalDate date) {
        if (date.isBefore(FILM_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза раньше 28 декабря 1895 года");
        }
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        return filmStorage.getPopularFilms(count, genreId, year);
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

    public List<Film> getCommonFilms(long userId, long friendId) {
        if (!userStorage.isExists(userId)) {
            throw new NotFoundException(ERR_USER + userId);
        } else if (!userStorage.isExists(friendId)) {
            throw new NotFoundException(ERR_USER + friendId);
        } else {
            return filmStorage.getCommonFilms(userId, friendId);
        }
    }

    public List<Film> getFilmsBySearch(String query, String by) {
        return filmStorage.getFilmsBySearch(query, by);
    }
}
