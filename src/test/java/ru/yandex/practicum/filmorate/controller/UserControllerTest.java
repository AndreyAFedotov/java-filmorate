package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты UserController")
public class UserControllerTest {
    public static final String ERR_COUNT = "Не верное количество ошибок";
    public static final String NEED_TRUE = "Должен был пройти валидацию";
    public static final String NEED_FALSE = "Не должен был пройти валидацию";
    public static final String EMAIL = "andrew@robot.com";
    public static final String LOGIN = "Andrew";
    public static final String NAME = "Andrew Robot";
    public static final String DATE = "1999-12-28";
    private Validator validator;

    @BeforeEach
    public void init() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    @DisplayName("Нормальная валидация")
    void normalValidationTest() {
        final User user = User.builder()
                .email(EMAIL)
                .login(LOGIN)
                .name(NAME)
                .birthday(LocalDate.parse(DATE))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), NEED_TRUE);
    }

    @Test
    @DisplayName("Email = NULL")
    void nullEmailTest() {
        final User user = User.builder()
                .email(null)
                .login(LOGIN)
                .name(NAME)
                .birthday(LocalDate.parse(DATE))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(2, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Пустой Email")
    void emptyEmailTest() {
        final User user = User.builder()
                .email("")
                .login(LOGIN)
                .name(NAME)
                .birthday(LocalDate.parse(DATE))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(1, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Не верный формат Email")
    void wrongFormatEmailTest() {
        final User user = User.builder()
                .email("это-неправильный?эмейл@")
                .login(LOGIN)
                .name(NAME)
                .birthday(LocalDate.parse(DATE))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(1, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Логин = NULL")
    void nullLoginTest() {
        final User user = User.builder()
                .email(EMAIL)
                .login(null)
                .name(NAME)
                .birthday(LocalDate.parse(DATE))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(2, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Пустой логин")
    void emptyLoginTest() {
        final User user = User.builder()
                .email(EMAIL)
                .login("")
                .name(NAME)
                .birthday(LocalDate.parse(DATE))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(2, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("День рождения в будущем")
    void birthdayTest() {
        final User user = User.builder()
                .email(EMAIL)
                .login(LOGIN)
                .name(NAME)
                .birthday(LocalDate.now().plusDays(1))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(1, violations.size(), ERR_COUNT);
    }

    @Test
    @DisplayName("Логин содержит пробелы")
    void spaceInLoginTest() {
        final User user = User.builder()
                .email(EMAIL)
                .login("one two")
                .name(NAME)
                .birthday(LocalDate.parse(DATE))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), NEED_FALSE);
        assertEquals(1, violations.size(), ERR_COUNT);
    }

//    @Test
//    @DisplayName("Логин в качестве имени")
//    void nameLoginTest() {
//        final User user = User.builder()
//                .email(EMAIL)
//                .login(LOGIN)
//                .name("")
//                .birthday(LocalDate.parse(DATE))
//                .build();
//        UserController userCnt = new UserController();
//        final User resUser = userCnt.createUser(user);
//        assertEquals(LOGIN, resUser.getName(), "Имя не обновилось из Логина");
//    }
}
