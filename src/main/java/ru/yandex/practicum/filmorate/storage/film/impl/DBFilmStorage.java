package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.SQLWorkException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
import java.util.stream.Collectors;

@Component("DBFilmStorage")
@Primary
@Slf4j
public class DBFilmStorage implements FilmStorage {
    private static final String FILM_ID = "FILM_ID";
    private static final String MPA_ID = "MPA_ID";
    private static final String GENRE_ID = "GENRE_ID";
    private static final String DIRECTOR_ID = "DIRECTOR_ID";
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
            setAdvFilmData(film);
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
            String sqlQuery = "insert into FILMS_DIRECTORS(FILM_ID, DIRECTOR_ID) values (?, ?)";
            for (Director director : film.getDirectors()) {
                jdbcTemplate.update(sqlQuery, film.getId(), director.getId());
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
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        String sqlQuery;
        List<Film> films = new ArrayList<>();

        if (genreId == 0 && year == 0) {
            log.info("Фильтрация популярных фильмов без параметров");
            sqlQuery = "SELECT F.FILM_ID, F.MPA_ID, F.NAME, F.DESCRIPTION, F.RELEASEDATE, F.DURATION, " +
                    "COUNT (L.USER_ID) as CNT from FILMS as F " +
                    "LEFT JOIN FILMS_LIKES L on F.FILM_ID = L.FILM_ID " +
                    "GROUP BY F.FILM_ID " +
                    "ORDER BY CNT desc " +
                    "LIMIT ?";
            films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), count);
        }
        if (genreId > 0 && year == 0) {
            log.info("Фильтрация популярных фильмов по жанрам");
            sqlQuery = "SELECT F.FILM_ID, F.MPA_ID, F.NAME, F.DESCRIPTION, F.RELEASEDATE, F.DURATION, "
                    + "COUNT (L.USER_ID) as CNT FROM FILMS as F "
                    + "LEFT JOIN FILMS_LIKES L on F.FILM_ID = L.FILM_ID "
                    + "LEFT JOIN FILMS_GENRES FG on F.FILM_ID = FG.FILM_ID "
                    + "WHERE FG.GENRE_ID=? "
                    + "GROUP BY F.FILM_ID, FG.GENRE_ID "
                    + "ORDER BY CNT DESC "
                    + "LIMIT ?";
            films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), genreId, count);
        }
        if (genreId == 0 && year > 0) {
            log.info("Фильтрация популярных фильмов по годам");
            sqlQuery = "SELECT F.FILM_ID, F.MPA_ID, F.NAME, F.DESCRIPTION, F.RELEASEDATE, F.DURATION, "
                    + "COUNT(L.USER_ID) as CNT FROM FILMS as F "
                    + "LEFT JOIN FILMS_LIKES L on F.FILM_ID = L.FILM_ID "
                    + "WHERE EXTRACT(YEAR FROM F.RELEASEDATE)=? "
                    + "GROUP BY F.FILM_ID "
                    + "ORDER BY CNT DESC "
                    + "LIMIT ?";
            films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), year, count);
        }
        if (genreId > 0 && year > 0) {
            log.info("Фильтрация популярных фильмов по жанрам и годам");
            sqlQuery = "SELECT F.FILM_ID, F.MPA_ID, F.NAME, F.DESCRIPTION, F.RELEASEDATE, F.DURATION, "
                    + "COUNT (L.USER_ID) as CNT from FILMS as F "
                    + "LEFT JOIN FILMS_LIKES L on F.FILM_ID = L.FILM_ID "
                    + "LEFT JOIN FILMS_GENRES FG on F.FILM_ID = FG.FILM_ID "
                    + "WHERE FG.GENRE_ID=? "
                    + "AND EXTRACT(YEAR FROM F.RELEASEDATE)=? "
                    + "GROUP BY F.FILM_ID, FG.GENRE_ID "
                    + "ORDER BY CNT DESC "
                    + "LIMIT ?";
            films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), genreId, year,
                    count);
        }
        if (genreId < 0 && year < 0) {
            throw new ValidationException(String.format(
                    "Неверные параметры фильтрации популярных фильмов"
                            + " genreId = %d and year = %d.", genreId, year));
        }

        for (Film film : films) {
            setAdvFilmData(film);
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

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        String sqlQuery = "select f.FILM_ID, f.MPA_ID, f.NAME, f.DESCRIPTION, f.RELEASEDATE, f.DURATION " +
                "from FILMS as f " +
                "inner join FILMS_LIKES as l1 ON f.FILM_ID = l1.FILM_ID and l1.USER_ID = ? " +
                "inner join FILMS_LIKES as l2 ON l1.FILM_ID = l2.FILM_ID and l2.USER_ID = ? ";
        List<Film> result = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), userId, friendId);
        for (Film film : result) {
            setAdvFilmData(film);
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
        Set<Long> filmsIds = getFilmsIdsForDirector(directorId);
        List<Film> films = new ArrayList<>();
        for (Long id : filmsIds) {
            films.add(getFilm(id));
        }
        if (year && likes) {
            return films.stream()
                    .sorted(Comparator.comparingInt(Film::getLikesCount))
                    .sorted(Comparator.comparing(Film::getReleaseDate))
                    .collect(Collectors.toList());
        } else if (year) {
            return films.stream()
                    .sorted(Comparator.comparing(Film::getReleaseDate))
                    .collect(Collectors.toList());
        } else if (likes) {
            return films.stream()
                    .sorted(Comparator.comparingInt(Film::getLikesCount))
                    .collect(Collectors.toList());
        } else {
            return films;
        }
    }

    @Override
    public List<Film> getFilmsBySearch(String query, String by) {
        List<Film> result = new ArrayList<>();
        boolean director = false;
        boolean title = false;

        String modQuery = "%" + query + "%";
        String sqlQuery = "SELECT f.film_id, f.mpa_id, f.name, f.description, f.releaseDate, f.duration, " +
                "COUNT(DISTINCT fl.user_id) AS amount_likes " +
                "FROM FILMS AS f " +
                "LEFT JOIN FILMS_LIKES AS fl ON f.film_id = fl.film_id " +
                "LEFT JOIN FILMS_DIRECTORS AS fd ON fd.film_id = f.film_id " +
                "LEFT JOIN DIRECTORS AS d ON d.director_id = fd.director_id " +
                "WHERE %s " +
                "GROUP BY f.film_id ORDER BY amount_likes DESC";
        for (String str : by.split(",")) {
            if (str.equals("director")) {
                director = true;
            } else if (str.equals("title")) {
                title = true;
            }
        }
        if (title && director) {
            sqlQuery = String.format(sqlQuery, "f.NAME ILIKE ? or d.NAME ILIKE ? ");
            result = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), modQuery, modQuery);
        } else if (director) {
            sqlQuery = String.format(sqlQuery, "d.NAME ILIKE ? ");
            result = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), modQuery);
        } else if (title) {
            sqlQuery = String.format(sqlQuery, "f.NAME ILIKE ?");
            result = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), modQuery);
        }
        for (Film film : result) {
            setAdvFilmData(film);
        }
        return result;
    }

    @Override
    public List<Film> getRecommendationsByUserId(long id) {
        final String sqlQuery = "select distinct FILM_ID " +
                                "from FILMS_LIKES " +
                                "where FILM_ID not in (select FILM_ID " +
                                        "from FILMS_LIKES " +
                                        "where USER_ID = ?) and USER_ID in (select USER_ID " +
                                                "from (select USER_ID, COUNT(FILM_ID) as COUNT_FILM " +
                                                        "from FILMS_LIKES " +
                                                        "where USER_ID != ? and FILM_ID in (select FILM_ID " +
                                                                "from FILMS_LIKES " +
                                                                "where  FILM_ID  in (select FILM_ID " +
                                                                        "from FILMS_LIKES " +
                                                                        "where USER_ID = ?)) " +
                                                        "group by USER_ID) as CF " +
                                        "where COUNT_FILM = (select MAX(COUNT_FILM)))";
        List<Long> filmIds = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilmId(rs), id, id, id);
        final String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        final String sqlQueryGetFilms = String.format("select * from FILMS where FILM_ID in (%s)", inSql);
        List<Film> films = jdbcTemplate.query(sqlQueryGetFilms, (rs, rowNum) -> makeFilm(rs), filmIds.toArray());
        for (Film film : films) {
            setAdvFilmData(film);
        }
        return films;
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

    private int getLikesCount(long id) {
        String sqlQuery = "select count(USER_ID) as CNT from FILMS_LIKES where FILM_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            return userRows.getInt("CNT");
        }
        return 0;
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

    private void setDirectorsDataForFilm(Film film) {
        String sqlQuery = "delete from FILMS_DIRECTORS where FILM_ID=?";
        jdbcTemplate.update(sqlQuery, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Director> directors = new HashSet<>(film.getDirectors());
            sqlQuery = "insert into FILMS_DIRECTORS (film_id, director_id) values (?, ? )";
            for (Director director : directors) {
                jdbcTemplate.update(sqlQuery, film.getId(), director.getId());
            }
        }
    }

    private void setAdvFilmData(Film film) {
        film.setMpa(getMpaDataByMpaId(film.getMpa().getId()));
        film.setGenres(getGenresDataForFilm(film.getId()));
        film.setLikesCount(getLikesCount(film.getId()));
        film.setDirectors(getDirectorsForFilm(film.getId()));
    }
}
