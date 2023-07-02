package ru.yandex.practicum.filmorate.storage.genre.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.SQLWorkException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("DBGenreStorage")
@Slf4j
public class DBGenreStorage implements GenreStorage {
    public static final String GENRE_ID = "GENRE_ID";
    public static final String NAME = "NAME";
    private final JdbcTemplate jdbcTemplate;

    public DBGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getGenres() {
        String sqlQuery = "select * from GENRES";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeGenre(rs));
    }

    @Override
    public Genre getGenre(long id) {
        String sqlQuery = "select * from GENRES where GENRE_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            return new Genre(userRows.getLong(GENRE_ID), userRows.getString(NAME));
        }
        return null;
    }

    private Genre makeGenre(ResultSet rs) {
        try {
            return new Genre(rs.getLong(GENRE_ID), rs.getString(NAME));
        } catch (SQLException e) {
            log.warn("Ошибка получения жанра: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения жанра");
        }
    }
}
