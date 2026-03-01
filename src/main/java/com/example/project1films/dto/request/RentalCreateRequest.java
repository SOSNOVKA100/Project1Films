package com.example.project1films.dto.request;

public class RentalCreateRequest {
    private Long userId;
    private Long movieId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
}