package ru.yandex.practicum.filmorate.storage.user.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "file:src/main/resources/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("Тесты UserStorage")
class UserStorageTests {
    private final UserStorage userStorage;

    @Test
    @DisplayName("Создание пользователя")
    void createUserTest() {
        User user = userStorage.createUser(createUser());
        assertNotNull(user, "Ошибка создания");
        assertEquals(1, user.getId(), "Ошибка присваивания ID");
    }

    @Test
    @DisplayName("Получение пользователя")
    void getUserTest() {
        userStorage.createUser(createUser());
        User user = userStorage.getUser(1);
        assertNotNull(user, "Ошибка получения");
        assertEquals(1, user.getId(), "Ошибочный пользователь");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getUsersTest() {
        userStorage.createUser(createUser());
        userStorage.createUser(createUser());
        List<User> users = userStorage.getUsers();
        assertNotNull(users, "Ошибка получение списка пользователей");
        assertEquals(2, users.size(), "Ошибочное количество пользователей");
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUserTest() {
        User user = userStorage.createUser(createUser());
        user.setLogin("test");
        User updatedUser = userStorage.updateUser(user);
        assertEquals("test", updatedUser.getLogin(), "Ошибка обновления пользователя");
    }

    @Test
    @DisplayName("Проверка существования")
    void isExistsTest() {
        userStorage.createUser(createUser());
        boolean result = userStorage.isExists(1);
        assertTrue(result, "Ошибка проверки существования");
    }

    @Test
    @DisplayName("Добавление друга")
    void addFriendTest() {
        userStorage.createUser(createUser());
        userStorage.createUser(createUser());
        User user = userStorage.addFriend(1, 2);
        assertEquals(1, user.getFriends().size());
    }

    @Test
    @DisplayName("Удаление друга")
    void deleteFromFriendsTest() {
        userStorage.createUser(createUser());
        userStorage.createUser(createUser());
        User user1 = userStorage.addFriend(1, 2);
        assertFalse(user1.getFriends().isEmpty(), "Ошибка добавления дружбы");
        User user2 = userStorage.deleteFromFriends(1, 2);
        assertTrue(user2.getFriends().isEmpty(), "Ошибка удаления дружбы");
    }

    @Test
    @DisplayName("Получение списка друзей")
    void getFriendsListForUserTest() {
        userStorage.createUser(createUser());
        userStorage.createUser(createUser());
        userStorage.addFriend(1, 2);
        List<User> friends = userStorage.getFriendsListForUser(1);
        assertEquals(1, friends.size(), "Не верное количество друзей");
        assertEquals(2, friends.get(0).getId(), "Ошибочный ID друга");
    }

    @Test
    @DisplayName("Общий друг")
    void getMutualFriendsTest() {
        userStorage.createUser(createUser());
        userStorage.createUser(createUser());
        userStorage.createUser(createUser());
        userStorage.addFriend(1, 3);
        userStorage.addFriend(2, 3);
        List<User> friends = userStorage.getMutualFriends(1, 2);
        assertEquals(1, friends.size(), "Не верное количество друзей");
        assertEquals(3, friends.get(0).getId(), "Ошибочный ID друга");
    }

    @Test
    @DisplayName("Взаимная дружба")
    void twoFriendsTest() {
        userStorage.createUser(createUser());
        userStorage.createUser(createUser());
        userStorage.addFriend(1, 2);
        userStorage.addFriend(2, 1);
        userStorage.deleteFromFriends(1, 2);
        User user = userStorage.getUser(1);
        assertTrue(user.getFriends().isEmpty(), "Ошибка списка друзей");
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
