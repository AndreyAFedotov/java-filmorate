package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long filmId = 0;

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNewId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        log.info("Обновлен фильм: {}", film.getName());
        return film;
    }

    @Override
    public boolean isExists(long id) {
        return films.containsKey(id);
    }

    @Override
    public Film setLikeToFilm(long id, long userId) {
        Film film = films.get(id);
        Set<Long> likes = film.getLikes();
        if (likes == null) {
            likes = new HashSet<>();
        }
        likes.add(userId);
        film.setLikes(likes);
        int likesCount = film.getLikesCount();
        likesCount++;
        film.setLikesCount(likesCount);
        log.info("Пользователь " + userId + " повставал лайк фильму " + id);
        return film;
    }

    @Override
    public Film deleteLikeFromFilm(long id, long userId) {
        Film film = films.get(id);
        Set<Long> likes = film.getLikes();
        if (likes == null || !likes.contains(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не ставил лайк фильму " + id);
        }
        likes.remove(userId);
        film.setLikes(likes);
        int likesCount = film.getLikesCount();
        likesCount--;
        film.setLikesCount(likesCount);
        log.info("Пользователь " + userId + " убрал лайк с фильма " + id);
        return film;
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        List<Film> filmsList = new ArrayList<>(films.values());
        Collections.reverse(filmsList);
        log.info("Запрос популярных фильмов. Количество: " + count);
        return filmsList.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Film getFilm(long id) {
        log.info("Запрос фильма: " + id);
        return films.get(id);
    }

    private long getNewId() {
        return ++filmId;
    }
}
