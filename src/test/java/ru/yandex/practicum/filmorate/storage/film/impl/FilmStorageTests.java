package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "file:src/main/resources/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("Тесты FilmStorage")
class FilmStorageTests {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Test
    @DisplayName("Создание фильма")
    void createFilmTest() {
        Film film = filmStorage.createFilm(createFilm());
        assertNotNull(film, "Ошибка создания фильма");
        assertEquals(1, film.getId(), "Ошибка присвоения ID");
    }

    @Test
    @DisplayName("Получение фильма")
    void getFilmTest() {
        filmStorage.createFilm(createFilm());
        Film film = filmStorage.getFilm(1);
        assertNotNull(film, "Ошибка получения фильма");
        assertEquals(1, film.getId(), "Ошибка ID фильма");
    }

    @Test
    @DisplayName("Получение списка фильмов")
    void getFilmsTest() {
        filmStorage.createFilm(createFilm());
        filmStorage.createFilm(createFilm());
        List<Film> films = filmStorage.getFilms();
        assertEquals(2, films.size(), "Ошибка получения списка");
    }

    @Test
    @DisplayName("Обновление фильма")
    void updateFilmTest() {
        filmStorage.createFilm(createFilm());
        Film film = filmStorage.getFilm(1);
        film.setName("test");
        Film result = filmStorage.updateFilm(film);
        assertEquals("test", result.getName(), "Ошибка обновления");
    }

    @Test
    @DisplayName("Проверка существования")
    void isExistsTest() {
        filmStorage.createFilm(createFilm());
        boolean result = filmStorage.isExists(1);
        assertTrue(result, "Ошибка проверки существования");
    }

    @Test
    @DisplayName("Поставить лайк")
    void setLikeToFilmTest() {
        User user = userStorage.createUser(createUser());
        Film film = filmStorage.createFilm(createFilm());
        filmStorage.setLikeToFilm(film.getId(), user.getId());
        Film result = filmStorage.getFilm(film.getId());
        assertEquals(1, result.getLikesCount(), "Ошибка установки лайка");
    }

    @Test
    @DisplayName("Удаление лайка")
    void deleteLikeFromFilmTest() {
        User user = userStorage.createUser(createUser());
        Film film = filmStorage.createFilm(createFilm());
        filmStorage.setLikeToFilm(film.getId(), user.getId());
        Film fTwo = filmStorage.getFilm(film.getId());
        assertEquals(1, fTwo.getLikesCount(), "Ошибка установки лайка");
        filmStorage.deleteLikeFromFilm(film.getId(), user.getId());
        Film result = filmStorage.getFilm(film.getId());
        assertEquals(0, result.getLikesCount(), "Ошибка удаления лайка");
    }

    @Test
    @DisplayName("Популярные")
    void getPopularFilmsTest() {
        filmStorage.createFilm(createFilm());
        filmStorage.createFilm(createFilm());
        filmStorage.createFilm(createFilm());
        List<Film> films = filmStorage.getPopularFilms(2, 0, 0);
        assertEquals(2, films.size(), "Ошибка списка популярных ");
    }


    private Film createFilm() {
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, null));
        genres.add(new Genre(2, null));

        return Film.builder()
                .name("Bicentennial Man")
                .description("One robot's 200 year journey to become an ordinary man")
                .releaseDate(LocalDate.parse("1999-12-28"))
                .duration(126)
                .mpa(new Mpa(2, null, null))
                .genres(genres)
                .build();
    }

    private User createUser() {
        return User.builder()
                .email("andrew@robot.com")
                .login("Andrew")
                .name("Andrew Robot")
                .birthday(LocalDate.parse("1999-12-28"))
                .build();
    }
}
