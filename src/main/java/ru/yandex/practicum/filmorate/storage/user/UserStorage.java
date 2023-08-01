package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    List<User> getUsers();

    User createUser(User user);

    User updateUser(User user);

    boolean isExists(long id);

    User addFriend(long id, long friendId);

    User deleteFromFriends(long id, long friendId);

    List<User> getFriendsListForUser(long id);

    List<User> getMutualFriends(long id, long otherId);

    User getUser(long id);

    User deleteUser(long id);
}
