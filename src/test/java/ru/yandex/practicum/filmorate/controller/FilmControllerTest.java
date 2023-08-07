package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.director.impl.DBDirectorStorage;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.event.impl.DBEventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.impl.DBFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.impl.DBUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты FilmController")
public class FilmControllerTest {
    public static final String NEED_TRUE = "Должен был пройти валидацию";
    public static final String NEED_FALSE = "Не должен был пройти валидацию";
    public static final String ERR_COUNT = "Не верное количество ошибок";
    public static final String NAME = "Bicentennial Man";
    public static final String DESCRIPTION = "One robot's 200 year journey to become an ordinary man";
    public static final String DATE = "1999-12-28";
    public static final int DURATION = 126;
    private Validator validator;

    @BeforeEach
    public void init() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    @DisplayName("Нормальная валидация")
    void normalValidationTest() {
        final Film film = Film.builder()
                .name(NAME)
                .description(DESCRIPTION)
                .releaseDate(LocalDate.parse(DATE))
                .duration(DURATION)
                .mpa(new Mpa(2, null, null))
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), NEED_TRUE);
    }

    @Test
    @DisplayName("Имя = NULL")
    void nullNameTest() {
        final Film film = Film.builder()
                .name(null)
                .description(DESCRIPTION)
                .releaseDate(LocalDate.parse(DATE))
                .duration(DURATION)
                .mpa(new Mpa(2, null, null))
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(2, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Имя пустое")
    void emptyNameTest() {
        final Film film = Film.builder()
                .name("")
                .description(DESCRIPTION)
                .releaseDate(LocalDate.parse(DATE))
                .duration(DURATION)
                .mpa(new Mpa(2, null, null))
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(1, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Описание более 200 символов")
    void descrLenTest() {
        final Film film = Film.builder()
                .name(NAME)
                .description("1234567890123456789012345678901234567890123456789012345678901234567890" +
                        "12345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                        "12345678901234567890123456789012345678901234567890" + "!")
                .releaseDate(LocalDate.parse(DATE))
                .duration(DURATION)
                .mpa(new Mpa(2, null, null))
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        Assertions.assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(1, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Отрицательная продолжительность")
    void negativeDurationTest() {
        final Film film = Film.builder()
                .name(NAME)
                .description(DESCRIPTION)
                .releaseDate(LocalDate.parse(DATE))
                .duration(-1)
                .mpa(new Mpa(2, null, null))
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(1, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Дата релиза")
    void releaseDateTest() {
        final Film film = Film.builder()
                .name(NAME)
                .description(DESCRIPTION)
                .releaseDate(LocalDate.parse("1895-12-27"))
                .duration(DURATION)
                .mpa(new Mpa(2, null, null))
                .build();
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        FilmStorage filmSt = new DBFilmStorage(jdbcTemplate, new NamedParameterJdbcTemplate(jdbcTemplate));
        UserStorage userSt = new DBUserStorage(new JdbcTemplate());
        DirectorStorage dirSt = new DBDirectorStorage(new JdbcTemplate());
        EventStorage eventSt = new DBEventStorage(new JdbcTemplate());
        FilmService filmSv = new FilmService(filmSt, userSt, dirSt, eventSt);
        FilmController filmCnt = new FilmController(filmSv);
        Throwable thrown = assertThrows(ValidationException.class, () ->
                filmCnt.createFilm(film));
        assertEquals("Дата релиза раньше 28 декабря 1895 года", thrown.getMessage());
    }
}
