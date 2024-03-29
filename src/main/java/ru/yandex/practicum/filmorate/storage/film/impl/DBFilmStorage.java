package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.SQLWorkException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("DBFilmStorage")
@Primary
@Slf4j
public class DBFilmStorage implements FilmStorage {
    private static final String FILM_ID = "FILM_ID";
    private static final String MPA_ID = "MPA_ID";
    private static final String GENRE_ID = "GENRE_ID";
    private static final String DIRECTOR_ID = "DIRECTOR_ID";
    private static final String NAME = "NAME";
    private static final String MPA_NAME = "MPA_NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String MPA_DESCRIPTION = "MPA_DESCRIPTION";
    private static final String RELEASEDATE = "RELEASEDATE";
    private static final String DURATION = "DURATION";
    private static final String CNT = "CNT";
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public DBFilmStorage(JdbcTemplate jdbcTemplate,
                         NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedParameterJdbcTemplate;

    }

    @Override
    public List<Film> getFilms() {
        String sqlQuery2 = "select f.FILM_ID, " +
                "       f.MPA_ID, " +
                "       f.NAME, " +
                "       f.DESCRIPTION, " +
                "       f.RELEASEDATE, " +
                "       f.DURATION, " +
                "       m.NAME as MPA_NAME, " +
                "       m.DESCRIPTION as MPA_DESCRIPTION, " +
                "       AVG (fl.MARK) as CNT " +
                "from FILMS as f " +
                "left join MPAS as m on f.MPA_ID = M.MPA_ID " +
                "left join FILMS_LIKES as fl on f.FILM_ID = FL.FILM_ID " +
                "group by f.FILM_ID;";

        List<Film> films = jdbcTemplate.query(sqlQuery2, (rs, rowNum) -> makeFilmOptimized(rs));
        for (Film film : films) {
            setAdvFilmDataLow(film);
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
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            List<Object[]> batch = new ArrayList<>();
            for (Director director : film.getDirectors()) {
                Object[] values = new Object[]{film.getId(), director.getId()};
                batch.add(values);
            }
            jdbcTemplate.batchUpdate("insert into FILMS_DIRECTORS(FILM_ID, DIRECTOR_ID) values (?, ?)", batch);
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
        setDirectorsDataForFilm(film);
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
    public Film setLikeToFilm(long id, long userId, int mark) {
        String sqlQuery = "merge into FILMS_LIKES (FILM_ID, USER_ID, MARK) values (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, id, userId, mark);
        return getFilm(id);
    }

    @Override
    public Film deleteLikeFromFilm(long id, long userId) {
        String sqlQuery = "delete from FILMS_LIKES where FILM_ID=? and USER_ID=?";
        jdbcTemplate.update(sqlQuery, id, userId);
        return getFilm(id);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        String sqlQuery = "SELECT F.FILM_ID, F.MPA_ID, F.NAME, F.DESCRIPTION, F.RELEASEDATE, F.DURATION, "
                + "(SUM (MARK) / COUNT(USER_ID)) as CNT, "
                + "m.NAME as MPA_NAME, m.DESCRIPTION as MPA_DESCRIPTION "
                + "from FILMS as F "
                + "LEFT JOIN FILMS_LIKES L on F.FILM_ID = L.FILM_ID "
                + "LEFT JOIN MPAS as m on F.MPA_ID = m.MPA_ID "
                + "%s "
                + "WHERE %s "
                + "GROUP BY F.FILM_ID %s "
                + "ORDER BY CNT desc "
                + "LIMIT ?";
        String joinGenresQuery = genreId > 0 ? "LEFT JOIN FILMS_GENRES FG on F.FILM_ID = FG.FILM_ID " : "";
        String whereCondition;
        String groupByGenre = "";
        if (genreId > 0 && year > 0) {
            whereCondition = "FG.GENRE_ID=? AND EXTRACT(YEAR FROM F.RELEASEDATE)=?";
            groupByGenre = ", FG.GENRE_ID";
        } else if (genreId > 0) {
            whereCondition = "FG.GENRE_ID=?";
            groupByGenre = ", FG.GENRE_ID";
        } else if (year > 0) {
            whereCondition = "EXTRACT(YEAR FROM F.RELEASEDATE)=?";
        } else {
            whereCondition = "1=1";
        }
        String formattedSql = String.format(sqlQuery, joinGenresQuery, whereCondition, groupByGenre);
        List<Object> queryParams = new ArrayList<>();
        if (genreId > 0) queryParams.add(genreId);
        if (year > 0) queryParams.add(year);
        queryParams.add(count);
        List<Film> films = jdbcTemplate.query(formattedSql, (rs, rowNum) -> makeFilmOptimized(rs), queryParams.toArray());
        for (Film film : films) {
            setAdvFilmDataLow(film);
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
                    getDirectorsForFilm(userRows.getLong(FILM_ID)),
                    getRating(userRows.getLong(FILM_ID)));
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

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        String sqlQuery = "select f.FILM_ID, f.MPA_ID, f.NAME, f.DESCRIPTION, f.RELEASEDATE, f.DURATION, " +
                "m.NAME AS MPA_NAME, m.DESCRIPTION as MPA_DESCRIPTION, count(flc.USER_ID) as CNT " +
                "from FILMS as f " +
                "inner join FILMS_LIKES as l1 ON f.FILM_ID = l1.FILM_ID and l1.USER_ID = ? and l1.MARK > 5 " +
                "join FILMS_LIKES as l2 ON l1.FILM_ID = l2.FILM_ID and l2.USER_ID = ? and l2.MARK > 5 " +
                "left join MPAS as m ON m.MPA_ID = f.MPA_ID " +
                "left join FILMS_LIKES as flc ON flc.FILM_ID = f.FILM_ID " +
                "group by f.FILM_ID";
        List<Film> result = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilmOptimized(rs), userId, friendId);
        for (Film film : result) {
            setAdvFilmDataLow(film);
        }
        return result;
    }

    @Override
    public List<Film> getDirectorsFilms(long directorId, Set<String> sortBy) {
        boolean likes = false;
        boolean year = false;

        if (sortBy != null && !sortBy.isEmpty()) {
            for (String srt : sortBy) {
                if (srt.equals("year")) {
                    year = true;
                } else if (srt.equals("likes")) {
                    likes = true;
                }
            }
        }
        SqlParameterSource parameters = new MapSqlParameterSource("ids", getFilmsIdsForDirector(directorId));
        String sqlQuery = "select f.FILM_ID, " +
                "       f.MPA_ID, " +
                "       f.NAME, " +
                "       f.DESCRIPTION, " +
                "       f.RELEASEDATE, " +
                "       f.DURATION, " +
                "       m.NAME as MPA_NAME, " +
                "       m.DESCRIPTION as MPA_DESCRIPTION, " +
                "       AVG (fl.MARK) as CNT " +
                "from FILMS as f " +
                "left join MPAS as m on f.MPA_ID = M.MPA_ID " +
                "left join FILMS_LIKES as fl on f.FILM_ID = FL.FILM_ID " +
                "where f.FILM_ID in (:ids) " +
                "group by f.FILM_ID ";
        if (year && likes) {
            sqlQuery += "order by f.RELEASEDATE, CNT ";
        } else if (year) {
            sqlQuery += "order by f.RELEASEDATE ";
        } else if (likes) {
            sqlQuery += "order by CNT ";
        }
        List<Film> result = namedJdbcTemplate.query(sqlQuery, parameters, (rs, rowNum) -> makeFilmOptimized(rs));
        for (Film film : result) {
            setAdvFilmDataLow(film);
        }
        return result;
    }

    @Override
    public List<Film> getFilmsBySearch(String query, String by) {
        List<Film> result = new ArrayList<>();
        boolean director = false;
        boolean title = false;

        String modQuery = "%" + query + "%";
        String sqlQuery = "SELECT f.film_id, f.mpa_id, f.name, f.description, f.releaseDate, f.duration, " +
                "m.NAME as MPA_NAME, m.DESCRIPTION as MPA_DESCRIPTION, " +
                "AVG (fl.MARK) as CNT, " +
                "COUNT(fl.USER_ID) as num_mark " +
                "FROM FILMS AS f " +
                "LEFT JOIN FILMS_LIKES AS fl ON f.film_id = fl.film_id " +
                "LEFT JOIN FILMS_DIRECTORS AS fd ON fd.film_id = f.film_id " +
                "LEFT JOIN DIRECTORS AS d ON d.director_id = fd.director_id " +
                "LEFT JOIN MPAS AS m ON m.MPA_ID = f.MPA_ID " +
                "WHERE %s " +
                "GROUP BY f.film_id ORDER BY num_mark DESC";
        for (String str : by.split(",")) {
            if (str.equals("director")) {
                director = true;
            } else if (str.equals("title")) {
                title = true;
            }
        }
        if (title && director) {
            sqlQuery = String.format(sqlQuery, "f.NAME ILIKE ? or d.NAME ILIKE ? ");
            result = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilmOptimized(rs), modQuery, modQuery);
        } else if (director) {
            sqlQuery = String.format(sqlQuery, "d.NAME ILIKE ? ");
            result = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilmOptimized(rs), modQuery);
        } else if (title) {
            sqlQuery = String.format(sqlQuery, "f.NAME ILIKE ?");
            result = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilmOptimized(rs), modQuery);
        }
        for (Film film : result) {
            setAdvFilmDataLow(film);
        }
        return result;
    }

    @Override
    public List<Film> getRecommendationsByUserId(long id) {
        final String sqlQuery = "select distinct FILM_ID, AVG (MARK) as RATE " +
                "from FILMS_LIKES " +
                "where FILM_ID not in (select FILM_ID " +
                "                      from FILMS_LIKES " +
                "                      where USER_ID = ?) " +
                "  and USER_ID in (select USER_ID " +
                "                  from (select USER_ID, COUNT(FILM_ID) as COUNT_FILM " +
                "                        from FILMS_LIKES " +
                "                        where USER_ID != ? and MARK > 5 " +
                "                        and FILM_ID in (select FILM_ID " +
                "                                        from FILMS_LIKES " +
                "                                        where MARK > 5 " +
                "                                        and FILM_ID  in (select FILM_ID " +
                "                                                         from FILMS_LIKES " +
                "                                                         where mark > 5 and USER_ID = ?)) " +
                "                        group by USER_ID) as CF " +
                "                  where COUNT_FILM = (select MAX(COUNT_FILM))) " +
                "group by FILM_ID " +
                "having RATE > 5";
        List<Long> filmIds = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilmId(rs), id, id, id);
        return getFilmsByIds(filmIds);
    }

    private Set<Long> getFilmsIdsForDirector(long directorId) {
        String sqlQuery = "select FILM_ID from FILMS_DIRECTORS where DIRECTOR_ID=?";
        List<Long> films = (jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilmId(rs), directorId));
        if (films.isEmpty()) {
            return new HashSet<>();
        } else {
            return new HashSet<>(films);
        }
    }

    private List<Film> getFilmsByIds(List<Long> ids) {
        SqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        String sqlQuery = "select f.FILM_ID, " +
                "       f.MPA_ID, " +
                "       f.NAME, " +
                "       f.DESCRIPTION, " +
                "       f.RELEASEDATE, " +
                "       f.DURATION, " +
                "       m.NAME as MPA_NAME, " +
                "       m.DESCRIPTION as MPA_DESCRIPTION, " +
                "       AVG (fl.MARK) as CNT " +
                "from FILMS as f " +
                "left join MPAS as m on f.MPA_ID = M.MPA_ID " +
                "left join FILMS_LIKES as fl on f.FILM_ID = FL.FILM_ID " +
                "where f.FILM_ID in (:ids) " +
                "group by f.FILM_ID";
        sqlQuery = String.format(sqlQuery, ids);
        List<Film> films = namedJdbcTemplate.query(sqlQuery, parameters, (rs, rowNum) -> makeFilmOptimized(rs));
        for (Film film : films) {
            setAdvFilmDataLow(film);
        }
        return films;
    }

    private Long makeFilmId(ResultSet rs) {
        try {
            return rs.getLong(FILM_ID);
        } catch (SQLException e) {
            log.warn("Ошибка получения id: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения id");
        }
    }


    private Mpa getMpaDataByMpaId(Long id) {
        String sqlQuery = "select NAME, DESCRIPTION from MPAS where MPA_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            return new Mpa(id, userRows.getString(NAME), userRows.getString(DESCRIPTION));
        }
        return null;
    }

    private Double getRating(long id) {
        String sqlQuery = "select AVG(MARK) as CNT from FILMS_LIKES where FILM_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            return userRows.getDouble("CNT");
        }
        return 0.0;
    }

    private Set<Director> getDirectorsForFilm(long id) {
        String sqlQuery = "select f.DIRECTOR_ID, d.NAME from FILMS_DIRECTORS AS f " +
                "left join DIRECTORS AS d ON f.DIRECTOR_ID = d.DIRECTOR_ID " +
                "where FILM_ID=?";

        List<Director> directors = (jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeDirector(rs), id));
        if (directors.isEmpty()) {
            return new HashSet<>();
        } else {
            return new HashSet<>(directors);
        }
    }

    private Director makeDirector(ResultSet rs) {
        try {
            return new Director(rs.getLong(DIRECTOR_ID), rs.getString(NAME));
        } catch (SQLException e) {
            log.warn("Ошибка получения режиссёра: {}", e.getMessage());
            throw new SQLWorkException("Ошибка получения режиссёра");
        }
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

    private Film makeFilmOptimized(ResultSet rs) {
        try {
            return new Film(rs.getLong(FILM_ID),
                    rs.getString(NAME),
                    rs.getString(DESCRIPTION),
                    rs.getDate(RELEASEDATE).toLocalDate(),
                    rs.getInt(DURATION),
                    new Mpa(rs.getLong(MPA_ID), rs.getString(MPA_NAME), rs.getString(MPA_DESCRIPTION)),
                    null, null, rs.getDouble(CNT));
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
            List<Object[]> batch = new ArrayList<>();
            for (Genre genre : genres) {
                Object[] values = new Object[]{film.getId(), genre.getId()};
                batch.add(values);
            }
            jdbcTemplate.batchUpdate("insert into FILMS_GENRES (FILM_ID, GENRE_ID) values (?, ? )", batch);
        }

    }

    private void setDirectorsDataForFilm(Film film) {
        String sqlQuery = "delete from FILMS_DIRECTORS where FILM_ID=?";
        jdbcTemplate.update(sqlQuery, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Director> directors = new HashSet<>(film.getDirectors());
            List<Object[]> batch = new ArrayList<>();
            for (Director director : directors) {
                Object[] values = new Object[]{film.getId(), director.getId()};
                batch.add(values);
            }
            jdbcTemplate.batchUpdate("insert into FILMS_DIRECTORS (film_id, director_id) values (?, ? )", batch);
        }
    }

    private void setAdvFilmDataLow(Film film) {
        film.setGenres(getGenresDataForFilm(film.getId()));
        film.setDirectors(getDirectorsForFilm(film.getId()));
    }
}

