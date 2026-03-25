package com.example.project1films.controller;

import com.example.project1films.dto.request.MovieCreateRequest;
import com.example.project1films.dto.request.MovieUpdateRequest;
import com.example.project1films.dto.response.MovieResponse;
import com.example.project1films.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/movies")
public class MovieControllerWithService {

    private final MovieService movieService;

    public MovieControllerWithService(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public MovieResponse createMovie(@RequestBody MovieCreateRequest request) {
        return movieService.createMovie(request);
    }



    @GetMapping("/{id}")
    public MovieResponse getMovie(@PathVariable Long id) {
        return movieService.getMovie(id);
    }

    @PutMapping("/{id}")
    public MovieResponse updateMovie(@PathVariable Long id,
                                     @RequestBody MovieUpdateRequest request) {
        return movieService.updateMovie(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }


    @GetMapping
    public Page<MovieResponse> getMovies(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return movieService.getMovies(genre, search, pageable);
    }


    @PostMapping("/mongo")
    public void saveToMongo(@RequestBody MovieCreateRequest request) {
        movieService.saveMovieToMongo(request);
    }

    @GetMapping("/mongo")
    public Page<MovieResponse> getFromMongo(Pageable pageable) {
        return movieService.getMoviesFromMongo(pageable);
    }


    // old @GetMapping
    // public List<MovieResponse> getAllMovies() {
    //    return movieService.getAllMovies();
    //  }
}