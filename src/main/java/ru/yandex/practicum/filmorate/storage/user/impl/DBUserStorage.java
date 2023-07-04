package ru.yandex.practicum.filmorate.storage.user.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.SQLWorkException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Component("DBUserStorage")
@Primary
@Slf4j
public class DBUserStorage implements UserStorage {
    private static final String USER_ID = "USER_ID";
    private static final String USER_ONE_ID = "USER_ONE_ID";
    private static final String USER_TWO_ID = "USER_TWO_ID";
    private static final String STATUS = "STATUS";
    private static final String EMAIL = "EMAIL";
    private static final String LOGIN = "LOGIN";
    private static final String NAME = "NAME";
    private static final String BIRTHDAY = "BIRTHDAY";
    private final JdbcTemplate jdbcTemplate;

    public DBUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        String sqlQuery = "select * from USERS";
        List<User> users = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeUser(rs));
        for (User user : users) {
            user.setFriends(new HashSet<>(getFriendsForId(user.getId())));
        }
        return users;
    }

    @Override
    public User createUser(User user) {
        String sqlQuery = "insert into USERS (EMAIL, LOGIN, NAME, BIRTHDAY) values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{USER_ID});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        try {
            user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        } catch (NullPointerException e) {
            log.warn("Ошибка создания пользователя" + e.getMessage());
            throw new SQLWorkException("Ошибка создания пользователя");
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sqlQuery = "update USERS set EMAIL=?, LOGIN=?, NAME=?, BIRTHDAY=? where USER_ID=?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return getUser(user.getId());
    }

    @Override
    public boolean isExists(long id) {
        String sqlQuery = "select USER_ID from USERS where USER_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            long readId = userRows.getLong(USER_ID);
            return readId == id;
        }
        return false;
    }

    @Override
    public User addFriend(long id, long friendId) {
        int newStatus = 0;
        String sqlQuery = "select * from FRIENDSHIPS where USER_ONE_ID=? and USER_TWO_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, friendId, id);
        if (userRows.next()) {
            long userOneId = userRows.getLong(USER_ONE_ID);
            long userTwoId = userRows.getLong(USER_TWO_ID);
            int status = userRows.getInt(STATUS);
            if (status == 0) {
                status = 1;
                sqlQuery = "update FRIENDSHIPS set STATUS=? where USER_ONE_ID=? and USER_TWO_ID=?";
                jdbcTemplate.update(sqlQuery, status, userOneId, userTwoId);
            }
            newStatus = 1;
        }
        sqlQuery = "insert into FRIENDSHIPS (USER_ONE_ID, USER_TWO_ID, STATUS) values (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, id, friendId, newStatus);
        return getUser(id);
    }

    @Override
    public User deleteFromFriends(long id, long friendId) {
        int currStatus = 0;
        String sqlQuery = "select STATUS from FRIENDSHIPS where USER_ONE_ID=? and USER_TWO_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id, friendId);
        if (userRows.next()) {
            currStatus = userRows.getInt(STATUS);
        }
        if (currStatus == 1) {
            sqlQuery = "update FRIENDSHIPS set STATUS=? where USER_ONE_ID=? and USER_TWO_ID=?";
            jdbcTemplate.update(sqlQuery, 0, friendId, id);
        }
        sqlQuery = "delete from FRIENDSHIPS where USER_ONE_ID=? and USER_TWO_ID=?";
        jdbcTemplate.update(sqlQuery, id, friendId);
        return getUser(id);
    }

    @Override
    public List<User> getFriendsListForUser(long id) {
        String sqlQuery = "select USER_TWO_ID from FRIENDSHIPS where USER_ONE_ID=?";
        return (jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFriendUser(rs), id));
    }

    @Override
    public List<User> getMutualFriends(long id, long otherId) {
        List<User> result = new ArrayList<>();
        String sqlQuery = "select USER_TWO_ID from FRIENDSHIPS where USER_ONE_ID=? " +
                "and USER_TWO_ID in (select USER_TWO_ID from FRIENDSHIPS where USER_ONE_ID=?)";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id, otherId);
        if (userRows.next()) {
            result.add(getUser(userRows.getLong(USER_TWO_ID)));
        }
        return result;
    }

    @Override
    public User getUser(long id) {
        String sqlQuery = "select * from USERS where USER_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            User user = new User(
                    userRows.getLong(USER_ID),
                    userRows.getString(EMAIL),
                    userRows.getString(LOGIN),
                    userRows.getString(NAME),
                    Objects.requireNonNull(userRows.getDate(BIRTHDAY)).toLocalDate(),
                    new HashSet<>());
            user.setFriends(new HashSet<>(getFriendsForId(user.getId())));
            return user;
        }
        return null;
    }

    private List<Long> getFriendsForId(Long id) {
        String sqlQuery = "select USER_TWO_ID from FRIENDSHIPS where USER_ONE_ID=?";
        return (jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFriendId(rs), id));
    }

    private User makeUser(ResultSet rs) {
        try {
            return new User(rs.getInt(USER_ID),
                    rs.getString(EMAIL),
                    rs.getString(LOGIN),
                    rs.getString(NAME),
                    rs.getDate(BIRTHDAY).toLocalDate(),
                    new HashSet<>());
        } catch (SQLException e) {
            log.warn("Ошибка получения пользователя: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения пользователя");
        }
    }

    private Long makeFriendId(ResultSet rs) {
        try {
            return rs.getLong(USER_TWO_ID);
        } catch (SQLException e) {
            log.warn("Ошибка получения id друга: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения id друга");
        }
    }

    private User makeFriendUser(ResultSet rs) {
        try {
            return getUser(rs.getLong(USER_TWO_ID));
        } catch (SQLException e) {
            log.warn("Ошибка получения: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения друга");
        }
    }
}
