package ru.yandex.practicum.filmorate.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventEnums.EventOperation;
import ru.yandex.practicum.filmorate.model.EventEnums.EventType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final EventStorage eventStorage;

    @Autowired
    public UserService(UserStorage userStorage, EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
    }

    public List<User> getUsers() {
        if (!userStorage.getUsers().isEmpty()) {
            return userStorage.getUsers();
        } else {
            throw new NotFoundException("Список пользователей пуст");
        }
    }

    public User createUser(User user) {
        if (StringUtils.isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        checkUserIsExist(user.getId());
        if (StringUtils.isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    public User deleteUser(long id) {
        checkUserIsExist(id);
        return userStorage.deleteUser(id);
    }

    public User addToFriends(long id, long friendId) {
        checkUserIsExist(id);
        checkUserIsExist(friendId);
        eventStorage.addEvent(id, EventType.FRIEND, EventOperation.ADD, friendId);
        return userStorage.addFriend(id, friendId);
    }

    public User deleteFromFriends(long id, long friendId) {
        checkUserIsExist(id);
        checkUserIsExist(friendId);
        eventStorage.addEvent(id, EventType.FRIEND, EventOperation.REMOVE, friendId);
        return userStorage.deleteFromFriends(id, friendId);
    }

    public List<User> getFriendsListForUser(long id) {
        checkUserIsExist(id);
        return userStorage.getFriendsListForUser(id);
    }

    public List<User> getMutualFriends(long id, long otherId) {
        checkUserIsExist(id);
        checkUserIsExist(otherId);
        return userStorage.getMutualFriends(id, otherId);
    }

    public void checkUserIsExist(long id) {
        if (!userStorage.isExists(id)) {
            throw new NotFoundException("Пользователя не существует: " + id);
        }
    }

    public User getUser(long id) {
        checkUserIsExist(id);
        return userStorage.getUser(id);
    }
}
