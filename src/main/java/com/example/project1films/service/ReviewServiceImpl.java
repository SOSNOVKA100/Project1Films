package com.example.project1films.service;

import com.example.project1films.dto.request.ReviewRequest;
import com.example.project1films.dto.response.ReviewResponse;
import com.example.project1films.entity.Movie;
import com.example.project1films.entity.Review;
import com.example.project1films.entity.User;
import com.example.project1films.exception.BusinessException;
import com.example.project1films.exception.ResourceNotFoundException;
import com.example.project1films.repository.MovieRepository;
import com.example.project1films.repository.ReviewRepository;
import com.example.project1films.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final AsyncNotificationService asyncNotificationService;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             MovieRepository movieRepository,
                             AsyncNotificationService asyncNotificationService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.asyncNotificationService = asyncNotificationService;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"reviews", "movies"}, allEntries = true)
    public ReviewResponse createReview(ReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));

        if (reviewRepository.existsByUserIdAndMovieId(user.getId(), movie.getId())) {
            throw new BusinessException("User has already reviewed this movie");
        }

        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUser(user);
        review.setMovie(movie);

        Review saved = reviewRepository.save(review);

        asyncNotificationService.logUserActionAsync(
                user.getId(), "CREATE_REVIEW", "Reviewed movie: " + movie.getTitle() + " with rating: " + request.getRating()
        );

        return convertToResponse(saved);
    }

    @Override
    @Cacheable(value = "reviews", key = "#id")
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return convertToResponse(review);
    }

    @Override
    public Page<ReviewResponse> getReviewsByMovie(Long movieId, Pageable pageable) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        return reviewRepository.findByMovieId(movieId, pageable).map(this::convertToResponse);
    }

    @Override
    public Page<ReviewResponse> getReviewsByUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return reviewRepository.findByUserId(userId, pageable).map(this::convertToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"reviews", "movies"}, key = "#id")
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updated = reviewRepository.save(review);

        asyncNotificationService.logUserActionAsync(
                review.getUser().getId(), "UPDATE_REVIEW", "Updated review for movie: " + review.getMovie().getTitle()
        );

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"reviews", "movies"}, key = "#id")
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);

        asyncNotificationService.logUserActionAsync(
                null, "DELETE_REVIEW", "Review deleted with id: " + id
        );
    }

    @Override
    public Double getMovieAverageRating(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        return reviewRepository.getAverageRatingByMovieId(movieId);
    }

    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse dto = new ReviewResponse();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getName());
        dto.setMovieId(review.getMovie().getId());
        dto.setMovieTitle(review.getMovie().getTitle());
        return dto;
    }
}