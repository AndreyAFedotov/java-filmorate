package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmService filmService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, FilmService filmService) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
    }

    public Review createReview(Review review) {
        checkUserAndFilm(review.getFilmId(), review.getUserId());
        checkPositive(review.getIsPositive());
        if (review.getContent() == null) {
            throw new ValidationException("Отзыв не может быть пустой");
        } else {
            return reviewStorage.createReview(review);
        }
    }

    public Review updateReview(Review review) {
        checkPositive(review.getIsPositive());
        checkReviewIsExist(review.getReviewId());
        if (review.getContent() == null) {
            throw new ValidationException("Отзыв не может быть пустой");
        } else {
            return reviewStorage.updateReview(review);
        }
    }

    public void deleteReviewById(Long reviewId) {
        checkReviewIsExist(reviewId);
        reviewStorage.deleteReviewById(reviewId);
    }

    public Review getReviewById(Long reviewId) {
        checkReviewIsExist(reviewId);
        return reviewStorage.getReviewById(reviewId);
    }

    public List<Review> getAllReviewsByFilmId(Long filmId, Integer count) {
        if (filmId != null) {
            filmService.checkFilmIsExist(filmId);
        }
        return reviewStorage.getAllReviewsByFilmId(filmId, count);
    }

    public void putLikeReview(Long reviewId, Long userId) {
        checkReviewIsExist(reviewId);
        filmService.checkUserIsExist(userId);
        reviewStorage.putLikeReview(reviewId, userId);
    }

    public void putDislikeReview(Long reviewId, Long userId) {
        checkReviewIsExist(reviewId);
        filmService.checkUserIsExist(userId);
        reviewStorage.putDislikeReview(reviewId, userId);
    }

    public void deleteLikeReview(Long reviewId, Long userId) {
        checkReviewIsExist(reviewId);
        filmService.checkUserIsExist(userId);
        reviewStorage.deleteLikeReview(reviewId, userId);
    }

    public void deleteDislikeReview(Long reviewId, Long userId) {
        checkReviewIsExist(reviewId);
        filmService.checkUserIsExist(userId);
        reviewStorage.deleteDislikeReview(reviewId, userId);
    }

    private void checkReviewIsExist(Long id) {
        if (!reviewStorage.isExists(id)) {
            throw new NotFoundException("Отзыв отсутствует: " + id);
        }
    }

    private void checkPositive(Boolean isPositive) {
        if (isPositive == null) {
            throw new ValidationException("Отсутствует статус отзыва");
        }
    }

    private void checkUserAndFilm(Long filmId, Long userId) {
        if (filmId == null) {
            throw new ValidationException("Отсутствует id фильма");
        }
        if (userId == null) {
            throw new ValidationException("Отсутствует id пользователя");
        }
        filmService.checkFilmIsExist(filmId);
        filmService.checkUserIsExist(userId);
    }
}
