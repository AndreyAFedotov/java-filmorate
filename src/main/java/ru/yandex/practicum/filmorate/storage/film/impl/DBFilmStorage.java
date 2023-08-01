package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.SQLWorkException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component("DBFilmStorage")
@Primary
@Slf4j
public class DBFilmStorage implements FilmStorage {
    private static final String FILM_ID = "FILM_ID";
    private static final String MPA_ID = "MPA_ID";
    private static final String GENRE_ID = "GENRE_ID";
    private static final String NAME = "NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String RELEASEDATE = "RELEASEDATE";
    private static final String DURATION = "DURATION";
    private final JdbcTemplate jdbcTemplate;

    public DBFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getFilms() {
        String sqlQuery2 = "select * from FILMS";
        List<Film> films = jdbcTemplate.query(sqlQuery2, (rs, rowNum) -> makeFilm(rs));
        for (Film film : films) {
            film.setMpa(getMpaDataByMpaId(film.getMpa().getId()));
            film.setGenres(getGenresDataForFilm(film.getId()));
            film.setLikesCount(getLikesCount(film.getId()));
        }
        return films;
    }

    @Override
    public Film createFilm(Film film) {
        String filmQuery = "insert into FILMS (MPA_ID, NAME, DESCRIPTION, RELEASEDATE, DURATION) values (?, ?, ?, ?, ? )";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(filmQuery, new String[]{FILM_ID});
            stmt.setLong(1, film.getMpa().getId());
            stmt.setString(2, film.getName());
            stmt.setString(3, film.getDescription());
            stmt.setDate(4, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(5, film.getDuration());
            return stmt;
        }, keyHolder);
        try {
            film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        } catch (NullPointerException e) {
            log.warn("Ошибка создания фильма" + e.getMessage());
            throw new SQLWorkException("Ошибка создания фильма");
        }
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sqlQuery = "insert into FILMS_GENRES (FILM_ID, GENRE_ID) values (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
            }
        }
        return getFilm(film.getId());
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlQuery = "update FILMS set MPA_ID=?, NAME=?, DESCRIPTION=?, RELEASEDATE=?, DURATION=? where FILM_ID=?";
        jdbcTemplate.update(sqlQuery,
                film.getMpa().getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());
        setGenresDataForFilm(film);
        return getFilm(film.getId());
    }

    @Override
    public boolean isExists(long id) {
        String sqlQuery = "select FILM_ID from FILMS where FILM_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            long readId = userRows.getLong(FILM_ID);
            return readId == id;
        }
        return false;
    }

    @Override
    public Film setLikeToFilm(long id, long userId) {
        int cnt = 0;
        String sqlQuery = "select count(FILM_ID) as CNT from FILMS_LIKES where FILM_ID=? and USER_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id, userId);
        if (userRows.next()) {
            cnt = userRows.getInt("CNT");
        }
        if (cnt == 0) {
            sqlQuery = "insert into FILMS_LIKES (FILM_ID, USER_ID) values (?, ?)";
            jdbcTemplate.update(sqlQuery, id, userId);
        }
        return getFilm(id);
    }

    @Override
    public Film deleteLikeFromFilm(long id, long userId) {
        String sqlQuery = "delete from FILMS_LIKES where FILM_ID=? and USER_ID=?";
        jdbcTemplate.update(sqlQuery, id, userId);
        return getFilm(id);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sqlQuery = "select F.FILM_ID, F.MPA_ID, F.NAME, F.DESCRIPTION, F.RELEASEDATE, F.DURATION, " +
                "COUNT (L.USER_ID) as CNT from FILMS as F " +
                "left join FILMS_LIKES L on F.FILM_ID = L.FILM_ID " +
                "group by F.FILM_ID " +
                "order by CNT desc " +
                "limit ?;";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), count);
        for (Film film : films) {
            film.setMpa(getMpaDataByMpaId(film.getMpa().getId()));
            film.setGenres(getGenresDataForFilm(film.getId()));
            film.setLikesCount(getLikesCount(film.getId()));
        }
        return films;
    }

    @Override
    public Film getFilm(long id) {
        String sqlQuery = "select * from FILMS where FILM_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            return new Film(userRows.getLong(FILM_ID),
                    userRows.getString(NAME),
                    userRows.getString(DESCRIPTION),
                    Objects.requireNonNull(userRows.getDate(RELEASEDATE)).toLocalDate(),
                    userRows.getInt(DURATION),
                    getMpaDataByMpaId(userRows.getLong(MPA_ID)),
                    getGenresDataForFilm(userRows.getLong(FILM_ID)),
                    new HashSet<>(),
                    getLikesCount(userRows.getLong(FILM_ID)));
        }
        return null;
    }

    @Override
    public Film deleteFilm(long id) {
        Film film = getFilm(id);
        String sqlQuery = "delete from FILMS where FILM_ID=?";
        jdbcTemplate.update(sqlQuery, id);
        return film;
    }

    private Mpa getMpaDataByMpaId(Long id) {
        String sqlQuery = "select NAME, DESCRIPTION from MPAS where MPA_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            return new Mpa(id, userRows.getString(NAME), userRows.getString(DESCRIPTION));
        }
        return null;
    }

    private int getLikesCount(long id) {
        String sqlQuery = "select count(USER_ID) as CNT from FILMS_LIKES where FILM_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            return userRows.getInt("CNT");
        }
        return 0;
    }

    private Set<Genre> getGenresDataForFilm(long id) {
        String sqlQuery = "select f.GENRE_ID, g.NAME from FILMS_GENRES AS f " +
                "left join GENRES AS g ON g.GENRE_ID = f.GENRE_ID " +
                "where FILM_ID=?";
        List<Genre> genres = (jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeGenre(rs), id));
        if (genres.isEmpty()) {
            return new HashSet<>();
        } else {
            return new HashSet<>(genres);
        }
    }

    private Genre makeGenre(ResultSet rs) {
        try {
            return new Genre(rs.getLong(GENRE_ID), rs.getString(NAME));
        } catch (SQLException e) {
            log.warn("Ошибка получения жанра: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения жанра");
        }
    }

    private Film makeFilm(ResultSet rs) {
        try {
            return new Film(rs.getLong(FILM_ID),
                    rs.getString(NAME),
                    rs.getString(DESCRIPTION),
                    rs.getDate(RELEASEDATE).toLocalDate(),
                    rs.getInt(DURATION),
                    new Mpa(rs.getLong(MPA_ID), null, null),
                    null, null, 0);
        } catch (SQLException e) {
            log.warn("Ошибка получения фильма: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения фильма");
        }
    }

    private void setGenresDataForFilm(Film film) {
        String sqlQuery = "delete from FILMS_GENRES where FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> genres = new HashSet<>(film.getGenres());
            sqlQuery = "insert into FILMS_GENRES (FILM_ID, GENRE_ID) values (?, ? )";
            for (Genre genre : genres) {
                jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
            }
        }
    }
}
