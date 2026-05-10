package com.example.project1films.service;

import com.example.project1films.dto.request.ReviewRequest;
import com.example.project1films.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);
    ReviewResponse getReviewById(Long id);
    Page<ReviewResponse> getReviewsByMovie(Long movieId, Pageable pageable);
    Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable);
    ReviewResponse updateReview(Long id, ReviewRequest request);
    void deleteReview(Long id);
    Double getMovieAverageRating(Long movieId);
}