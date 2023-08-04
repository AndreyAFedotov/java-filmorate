package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.enums.EventOperation;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmService filmService;
    private final EventStorage eventStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage, FilmService filmService, EventStorage eventStorage) {
        this.reviewStorage = reviewStorage;
        this.filmService = filmService;
        this.eventStorage = eventStorage;
    }

    public Review createReview(Review review) {
        checkUserAndFilm(review.getFilmId(), review.getUserId());
        checkPositive(review.getIsPositive());
        if (review.getContent() == null) {
            throw new ValidationException("Отзыв не может быть пустой");
        } else {
            Review newReview = reviewStorage.createReview(review);
            eventStorage.addEvent(newReview.getUserId(), EventType.REVIEW, EventOperation.ADD, newReview.getReviewId());
            return newReview;
        }
    }

    public Review updateReview(Review review) {
        checkPositive(review.getIsPositive());
        checkReviewIsExist(review.getReviewId());
        if (review.getContent() == null) {
            throw new ValidationException("Отзыв не может быть пустой");
        } else {
            Review newReview = reviewStorage.updateReview(review);
            Review oldReview = reviewStorage.getReviewById(review.getReviewId());
            eventStorage.addEvent(oldReview.getUserId(),
                    EventType.REVIEW, EventOperation.UPDATE, oldReview.getReviewId());
            return newReview;
        }
    }

    public void deleteReviewById(Long reviewId) {
        checkReviewIsExist(reviewId);
        Review delReview = reviewStorage.getReviewById(reviewId);
        reviewStorage.deleteReviewById(reviewId);
        eventStorage.addEvent(delReview.getUserId(), EventType.REVIEW, EventOperation.REMOVE, delReview.getReviewId());
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
