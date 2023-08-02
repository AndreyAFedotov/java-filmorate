package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping(value = "/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping(value = "/{reviewId}")
    public void deleteReviewById(@PathVariable Long reviewId) {
        reviewService.deleteReviewById(reviewId);
    }

    @GetMapping(value = "/{reviewId}")
    public Review getReviewById(@PathVariable Long reviewId) {
        return reviewService.getReviewById(reviewId);
    }

    @GetMapping
    public List<Review> getAllReviewsByFilmId(@RequestParam(value = "filmId", required = false) Long reviewId,
                                             @RequestParam(value = "count", defaultValue = "10") Integer count) {
        return reviewService.getAllReviewsByFilmId(reviewId, count);
    }

    @PutMapping(value = "/{reviewId}/like/{userId}")
    public void putLikeReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.putLikeReview(reviewId, userId);
    }

    @PutMapping(value = "/{reviewId}/dislike/{userId}")
    public void putDislikeReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.putDislikeReview(reviewId, userId);
    }

    @DeleteMapping(value = "/{reviewId}/like/{userId}")
    public void deleteLikeReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.deleteLikeReview(reviewId, userId);
    }

    @DeleteMapping(value = "/{reviewId}/dislike/{userId}")
    public void deleteDislikeReview(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.deleteDislikeReview(reviewId, userId);
    }
}
