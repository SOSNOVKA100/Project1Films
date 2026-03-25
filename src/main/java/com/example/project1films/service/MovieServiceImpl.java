package com.example.project1films.service;

import com.example.project1films.dto.request.MovieCreateRequest;
import com.example.project1films.dto.request.MovieUpdateRequest;
import com.example.project1films.dto.response.MovieResponse;
import com.example.project1films.entity.Movie;
import com.example.project1films.entity.mongo.MovieDocument;
import com.example.project1films.repository.MovieRepository;
import com.example.project1films.repository.mongo.MovieMongoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieMongoRepository movieMongoRepository;

    public MovieServiceImpl(MovieRepository movieRepository,
                            MovieMongoRepository movieMongoRepository) {
        this.movieRepository = movieRepository;
        this.movieMongoRepository = movieMongoRepository;
    }

    // ================= PostgreSQL =================

    @Override
    public MovieResponse createMovie(MovieCreateRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setGenre(request.getGenre());
        movie.setAvailable(request.getAvailable());

        Movie saved = movieRepository.save(movie);
        return mapToResponse(saved);
    }

    @Override
    public MovieResponse getMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return mapToResponse(movie);
    }

    @Override
    public MovieResponse updateMovie(Long id, MovieUpdateRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getGenre() != null) movie.setGenre(request.getGenre());
        if (request.getAvailable() != null) movie.setAvailable(request.getAvailable());

        Movie updated = movieRepository.save(movie);
        return mapToResponse(updated);
    }

    @Override
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    @Override
    public Page<MovieResponse> getMovies(
            String genre,
            String search,
            Pageable pageable
    ) {

        Specification<Movie> spec =
                (root, query, cb) -> cb.conjunction();

        if (genre != null && !genre.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("genre"), genre));
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("title")),
                            "%" + search.toLowerCase() + "%"
                    ));
        }

        return movieRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    // ================= MongoDB =================

    @Override
    public void saveMovieToMongo(MovieCreateRequest request) {
        MovieDocument doc = new MovieDocument();
        doc.setTitle(request.getTitle());
        doc.setGenre(request.getGenre());
        doc.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        movieMongoRepository.save(doc);
    }

    @Override
    public Page<MovieResponse> getMoviesFromMongo(Pageable pageable) {
        return movieMongoRepository.findAll(pageable)
                .map(this::mapToResponseMongo);
    }

    // ================= Mappers =================

    private MovieResponse mapToResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setGenre(movie.getGenre());
        response.setAvailable(movie.getAvailable());
        return response;
    }

    private MovieResponse mapToResponseMongo(MovieDocument doc) {
        MovieResponse response = new MovieResponse();
        response.setTitle(doc.getTitle());
        response.setGenre(doc.getGenre());
        response.setAvailable(doc.getAvailable());
        return response;
    }
}