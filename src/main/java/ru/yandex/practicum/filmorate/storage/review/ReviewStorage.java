package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReviewById(Long reviewId);

    Review getReviewById(Long reviewId);

    List<Review> getAllReviewsByFilmId(Long filmId, Integer count);

    void putLikeReview(Long reviewId, Long userId);

    void putDislikeReview(Long reviewId, Long userId);

    void deleteLikeReview(Long reviewId, Long userId);

    void deleteDislikeReview(Long reviewId, Long userId);

    boolean isExists(Long id);
}
