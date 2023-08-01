package ru.yandex.practicum.filmorate.storage.director.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.SQLWorkException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component("DBDirectorStorage")
@Primary
@Slf4j
public class DBDirectorStorage implements DirectorStorage {
    private static final String DIRECTOR_ID = "DIRECTOR_ID";
    private static final String NAME = "NAME";
    private final JdbcTemplate jdbcTemplate;

    public DBDirectorStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getDirectors() {
        String sqlQuery = "select * from DIRECTORS";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeDirector(rs));
    }

    @Override
    public Director getDirector(long id) {
        String sqlQuery = "select * from DIRECTORS where DIRECTOR_ID=?";
        SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (directorRows.next()) {
            return new Director(directorRows.getLong(DIRECTOR_ID), directorRows.getString(NAME));
        }
        return null;
    }

    @Override
    public Director createDirector(Director director) {
        String sqlQuery = "insert into DIRECTORS (NAME) values ( ? )";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(sqlQuery, new String[]{DIRECTOR_ID});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "update DIRECTORS set NAME=? where DIRECTOR_ID=?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        return getDirector(director.getId());
    }

    @Override
    public Director deleteDirector(long id) {
        Director director = getDirector(id);
        String sqlQuery = "delete from DIRECTORS where DIRECTOR_ID=?";
        jdbcTemplate.update(sqlQuery, id);
        return director;
    }

    @Override
    public boolean isExists(long id) {
        String sqlQuery = "select DIRECTOR_ID from DIRECTORS where DIRECTOR_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            long readId = userRows.getLong(DIRECTOR_ID);
            return readId == id;
        }
        return false;
    }

    private Director makeDirector(ResultSet rs) {
        try {
            return new Director(rs.getLong(DIRECTOR_ID), rs.getString(NAME));
        } catch (SQLException e) {
            log.warn("Ошибка получения режиссёра: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения режиссёра");
        }
    }
}
