package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long userId = 0;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        user.setId(getNewId());
        user.setFriends(new HashSet<>());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user.getName());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        users.put(user.getId(), user);
        log.info("Обновлен пользователь: {}", user.getName());
        return user;
    }

    @Override
    public boolean isExists(long id) {
        return users.containsKey(id);
    }

    @Override
    public User addFriend(long id, long friendId) {
        final Set<Long> friendsOne;
        final Set<Long> friendsTwo;

        final User userOne = users.get(id);
        final User userTwo = users.get(friendId);
        friendsOne = userOne.getFriends();
        friendsTwo = userTwo.getFriends();
        friendsOne.add(friendId);
        friendsTwo.add(id);
        userOne.setFriends(friendsOne);
        userTwo.setFriends(friendsTwo);
        log.info("Пользователь {} создал взаимную дружбу с {}.", id, friendId);
        return userOne;
    }

    @Override
    public User deleteFromFriends(long id, long friendId) {
        final User userOne = users.get(id);
        final User userTwo = users.get(friendId);
        final Set<Long> friendsOne = userOne.getFriends();
        final Set<Long> friendsTwo = userTwo.getFriends();
        friendsOne.remove(friendId);
        friendsTwo.remove(id);
        userOne.setFriends(friendsOne);
        userTwo.setFriends(friendsTwo);
        log.info("Пользователь {} удалил взаимную дружбу с {}.", id, friendId);
        return userOne;
    }

    @Override
    public List<User> getFriendsListForUser(long id) {
        log.info("Запрос списка друзей для пользователя: " + id);
        List<User> output = new ArrayList<>();
        for (Long user : users.get(id).getFriends()) {
            output.add(users.get(user));
        }
        return output;
    }

    @Override
    public List<User> getMutualFriends(long id, long otherId) {
        final User userOne = users.get(id);
        final User userTwo = users.get(otherId);
        if (userOne.getFriends().isEmpty() || userTwo.getFriends().isEmpty()) {
            log.info("У пользователей " + id + " и " + otherId + " нет общих друзей");
            return new ArrayList<>();
        }
        final Set<Long> friendsOne = new HashSet<>(userOne.getFriends());
        final Set<Long> friendsTwo = new HashSet<>(userTwo.getFriends());
        friendsOne.retainAll(friendsTwo);
        if (friendsOne.isEmpty()) {
            log.info("У пользователей " + id + " и " + otherId + " нет общих друзей");
            return new ArrayList<>();
        }
        List<User> result = new ArrayList<>();
        for (Long idUser : friendsOne) {
            result.add(users.get(idUser));
        }
        return result;
    }

    @Override
    public User getUser(long id) {
        return users.get(id);
    }

    private long getNewId() {
        return ++userId;
    }
}
