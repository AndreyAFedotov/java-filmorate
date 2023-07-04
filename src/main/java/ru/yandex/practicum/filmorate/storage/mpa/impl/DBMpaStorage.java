package ru.yandex.practicum.filmorate.storage.mpa.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.SQLWorkException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("DBMpaStorage")
@Slf4j
public class DBMpaStorage implements MpaStorage {
    private static final String MPA_ID = "MPA_ID";
    private static final String NAME = "NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private final JdbcTemplate jdbcTemplate;

    public DBMpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getMpas() {
        String sqlQuery = "select * from MPAS";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeMpa(rs));
    }

    @Override
    public Mpa getMpa(long id) {
        String sqlQuery = "select * from MPAS where MPA_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            return new Mpa(userRows.getLong(MPA_ID),
                    userRows.getString(NAME),
                    userRows.getString(DESCRIPTION));
        }
        return null;
    }

    private Mpa makeMpa(ResultSet rs) {
        try {
            return new Mpa(rs.getLong(MPA_ID),
                    rs.getString(NAME),
                    rs.getString(DESCRIPTION));
        } catch (SQLException e) {
            log.warn("Ошибка получения MPA: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения MPA");
        }
    }
}
