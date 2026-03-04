package com.example.project1films.controller;

import com.example.project1films.entity.Movie;
import com.example.project1films.repository.MovieRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
//@RequestMapping("/moviesold")
public class MovieController {

    private final MovieRepository repo;

    public MovieController(MovieRepository repo) {
        this.repo = repo;
    }

   // @GetMapping
    public List<Movie> getAllMovies() {
        return repo.findAll();
    }

   // @PostMapping
    public Movie createMovie(@RequestBody Movie movie) {
        return repo.save(movie);
    }
}
