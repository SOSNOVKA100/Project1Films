package com.example.project1films.service;


import com.example.project1films.dto.request.MovieCreateRequest;
import com.example.project1films.dto.request.MovieUpdateRequest;
import com.example.project1films.dto.response.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

public interface MovieService {
    MovieResponse createMovie(MovieCreateRequest request);
    MovieResponse getMovie(Long id);
    MovieResponse updateMovie(Long id, MovieUpdateRequest request);
    void deleteMovie(Long id);

    Page<MovieResponse> getMovies(String genre, String search, Pageable pageable);
}

