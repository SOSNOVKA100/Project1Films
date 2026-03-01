package com.example.project1films.service;

import com.example.project1films.dto.request.MovieCreateRequest;
import com.example.project1films.dto.request.MovieUpdateRequest;
import com.example.project1films.dto.response.MovieResponse;

import java.util.List;

public interface MovieService {
    MovieResponse createMovie(MovieCreateRequest request);
    List<MovieResponse> getAllMovies();
    MovieResponse getMovie(Long id);
    MovieResponse updateMovie(Long id, MovieUpdateRequest request);
    void deleteMovie(Long id);
}