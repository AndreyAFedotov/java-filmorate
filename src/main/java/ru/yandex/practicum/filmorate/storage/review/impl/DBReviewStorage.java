package ru.yandex.practicum.filmorate.storage.review.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewRowMapper;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class DBReviewStorage implements ReviewStorage {

    private static final Byte LIKE = 1;
    private static final Byte DISLIKE = -1;
    private final NamedParameterJdbcOperations jdbcOperations;
    private final ReviewRowMapper reviewRowMapper;


    public DBReviewStorage(NamedParameterJdbcOperations jdbcOperations, ReviewRowMapper reviewRowMapper) {
        this.jdbcOperations = jdbcOperations;
        this.reviewRowMapper = reviewRowMapper;
    }

    @Override
    public Review createReview(Review review) {
        String sqlQuery = "insert into REVIEWS (CONTENT, POSITIVE, USER_ID, FILM_ID) " +
                "values (:content, :positive, :userId, :filmId)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(sqlQuery, getMapParameter(review), keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return getReviewById(review.getReviewId());
    }

    @Override
    public Review updateReview(Review review) {
        final String sqlQuery = "update REVIEWS set CONTENT = :content, POSITIVE = :positive " +
                "where REVIEW_ID = :reviewId";
        jdbcOperations.update(sqlQuery, Map.of("content", review.getContent(),
                                                "positive", review.getIsPositive(),
                                                "reviewId", review.getReviewId()));
        return getReviewById(review.getReviewId());
    }

    @Override
    public void deleteReviewById(Long reviewId) {
        final String sqlQuery = "delete from REVIEWS where review_id = :reviewId";
        jdbcOperations.update(sqlQuery, Map.of("reviewId", reviewId));
    }

    @Override
    public Review getReviewById(Long reviewId) {
        final String sqlQuery = "select R.REVIEW_ID, R.CONTENT, R.POSITIVE, R.USER_ID, R.FILM_ID, " +
                "(select COALESCE(SUM(U.USEFUL_STATUS), 0) from USEFULS U where U.REVIEW_ID = R.REVIEW_ID) as USEFUL " +
                "from REVIEWS as R " +
                "where R.REVIEW_ID = :reviewId ";
        List<Review> reviews = jdbcOperations.query(sqlQuery,Map.of("reviewId", reviewId), reviewRowMapper);
        if (reviews.size() != 1) return null;
        return reviews.get(0);
    }

    @Override
    public List<Review> getAllReviewsByFilmId(Long filmId, Integer count) {
        if (filmId == null) {
            return getAllReviews(count);
        }
        final String sqlQuery = "select R.REVIEW_ID, R.CONTENT, R.POSITIVE, R.USER_ID, R.FILM_ID, " +
                "(select COALESCE(SUM(U.USEFUL_STATUS), 0) from USEFULS U where U.REVIEW_ID = R.REVIEW_ID) as USEFUL " +
                "from REVIEWS as R " +
                "where R.FILM_ID = :filmId order by USEFUL DESC " +
                "limit :count";
        return jdbcOperations.query(sqlQuery,
                Map.of("filmId", filmId,"count", count), reviewRowMapper);
    }

    @Override
    public void putLikeReview(Long reviewId, Long userId) {
        putLikeReviewOrDislike(reviewId, userId, LIKE);
    }

    @Override
    public void putDislikeReview(Long reviewId, Long userId) {
        putLikeReviewOrDislike(reviewId, userId, DISLIKE);
    }

    @Override
    public void deleteLikeReview(Long reviewId, Long userId) {
        deleteLikeReviewOrDislike(reviewId,userId);
    }

    private void deleteLikeReviewOrDislike(Long reviewId, Long userId) {
        final String sqlQuery = "delete from USEFULS " +
                "where REVIEW_ID = :reviewId and USER_ID = :userId";
        jdbcOperations.update(sqlQuery, Map.of("reviewId", reviewId,
                "userId", userId));
    }

    @Override
    public void deleteDislikeReview(Long reviewId, Long userId) {
        final String sqlQuery = "select USEFUL_STATUS FROM USEFULS " +
                "where REVIEW_ID = :reviewId and USER_ID = :userId";
        List<Byte> usefulStatus = jdbcOperations.query(sqlQuery,
                Map.of("reviewId", reviewId,
                        "userId", userId),
                (rs, roNum) -> rs.getByte("USEFUL_STATUS"));
        if (usefulStatus.isEmpty() || usefulStatus.get(0).equals(LIKE)) {
            throw new NotFoundException("Дизлайк: " + reviewId +
                    " пользователя: " + userId + " не найден");
        }
        deleteLikeReviewOrDislike(reviewId, userId);
    }

    @Override
    public boolean isExists(Long id) {
        final String sqlQuery = "select REVIEW_ID from REVIEWS where REVIEW_ID = :id";
        List<Long> reviewId = jdbcOperations.query(sqlQuery,
                Map.of("id", id), ((rs, rowNum) -> rs.getLong("REVIEW_ID")));
        return !reviewId.isEmpty() && reviewId.get(0).equals(id);
    }

    private void putLikeReviewOrDislike(Long reviewId, Long userId, Byte status) {
        String sqlQueryUseful = "select REVIEW_ID, USER_ID, USEFUL_STATUS from USEFULS " +
                "where REVIEW_ID = :reviewId and USER_ID = :userId";
        List<Useful> usefulList = jdbcOperations.query(sqlQueryUseful,
                Map.of("reviewId", reviewId, "userId",  userId),
                (rs, rowNum) -> new Useful(rs.getLong("REVIEW_ID"),
                        rs.getLong("USER_ID"),
                        rs.getByte("USEFUL_STATUS")));
        if (usefulList.isEmpty()) {
            String sqlQuery = "insert into USEFULS (REVIEW_ID, USER_ID, USEFUL_STATUS) " +
                    "values (:reviewId, :userId, :usefulStatus)";
            jdbcOperations.update(sqlQuery, Map.of("reviewId", reviewId,
                    "userId", userId,
                    "usefulStatus", status));
        } else if (!usefulList.get(0).status.equals(status)) {
            final String updateSqlQuery = "update USEFULS set USEFUL_STATUS = :usefulStatus " +
                    "where REVIEW_ID = :reviewId and USER_ID = :userId";
            jdbcOperations.update(updateSqlQuery, Map.of("usefulStatus", status,
                                                            "reviewId", reviewId,
                                                            "userId", userId));
        }
    }

    @AllArgsConstructor
    @Data
    private static class Useful {
        Long reviewId;
        Long userId;
        Byte status;
    }

    private MapSqlParameterSource getMapParameter(Review review) {
        MapSqlParameterSource map = new MapSqlParameterSource();
        map.addValue("content", review.getContent());
        map.addValue("positive", review.getIsPositive());
        map.addValue("userId", review.getUserId());
        map.addValue("filmId", review.getFilmId());
        return map;
    }

    private List<Review> getAllReviews(Integer count) {
        final String sqlQuery = "select R.REVIEW_ID, R.CONTENT, R.POSITIVE, R.USER_ID, R.FILM_ID, " +
                "(select COALESCE(SUM(U.USEFUL_STATUS), 0) from USEFULS U where U.REVIEW_ID = R.REVIEW_ID) as USEFUL " +
                "from REVIEWS as R " +
                "order by USEFUL DESC " +
                "limit :count";
        return jdbcOperations.query(sqlQuery,
                Map.of("count", count), reviewRowMapper);
    }
}


