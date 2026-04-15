package com.example.project1films.service;

import com.example.project1films.dto.request.MovieCreateRequest;
import com.example.project1films.dto.request.MovieUpdateRequest;
import com.example.project1films.dto.response.MovieResponse;
import com.example.project1films.entity.Movie;
import com.example.project1films.entity.mongo.MovieDocument;
import com.example.project1films.exception.DuplicateResourceException;
import com.example.project1films.exception.ResourceNotFoundException;
import com.example.project1films.repository.MovieRepository;
import com.example.project1films.repository.mongo.MovieMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieServiceImpl implements MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    private final MovieRepository movieRepository;
    private final MovieMongoRepository movieMongoRepository;
    private final AsyncNotificationService asyncNotificationService;

    public MovieServiceImpl(MovieRepository movieRepository,
                            MovieMongoRepository movieMongoRepository,
                            AsyncNotificationService asyncNotificationService) {
        this.movieRepository = movieRepository;
        this.movieMongoRepository = movieMongoRepository;
        this.asyncNotificationService = asyncNotificationService;
    }

    // ================= PostgreSQL =================

    @Override
    @Transactional
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse createMovie(MovieCreateRequest request) {
        logger.info("Creating movie with title: {}", request.getTitle());

        if (movieRepository.existsByTitle(request.getTitle())) {
            throw new DuplicateResourceException("Movie with title '" + request.getTitle() + "' already exists");
        }

        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setGenre(request.getGenre());
        movie.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        Movie saved = movieRepository.save(movie);
        logger.info("Movie created successfully with id: {}", saved.getId());

        MovieResponse response = mapToResponse(saved);

        asyncNotificationService.logUserActionAsync(
                null,
                "CREATE_MOVIE",
                "Movie created: " + saved.getTitle()
        );

        return response;
    }

    @Override
    @Cacheable(value = "movies", key = "#id")
    public MovieResponse getMovie(Long id) {
        logger.info("Fetching movie from DATABASE (not cache): id={}", id);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        return mapToResponse(movie);
    }

    @Override
    @Transactional
    @CachePut(value = "movies", key = "#id")
    public MovieResponse updateMovie(Long id, MovieUpdateRequest request) {
        logger.info("Updating movie with id: {}", id);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        StringBuilder changes = new StringBuilder();

        if (request.getTitle() != null && !request.getTitle().equals(movie.getTitle())) {
            if (movieRepository.existsByTitle(request.getTitle())) {
                throw new DuplicateResourceException("Movie with title '" + request.getTitle() + "' already exists");
            }
            changes.append("Title changed from '").append(movie.getTitle()).append("' to '").append(request.getTitle()).append("'; ");
            movie.setTitle(request.getTitle());
        }

        if (request.getGenre() != null && !request.getGenre().equals(movie.getGenre())) {
            changes.append("Genre changed from '").append(movie.getGenre()).append("' to '").append(request.getGenre()).append("'; ");
            movie.setGenre(request.getGenre());
        }

        if (request.getAvailable() != null && !request.getAvailable().equals(movie.getAvailable())) {
            changes.append("Availability changed from ").append(movie.getAvailable()).append(" to ").append(request.getAvailable()).append("; ");
            movie.setAvailable(request.getAvailable());
        }

        Movie updated = movieRepository.save(movie);
        logger.info("Movie updated successfully with id: {}", id);

        MovieResponse response = mapToResponse(updated);

        if (changes.length() > 0) {
            asyncNotificationService.logUserActionAsync(
                    null,
                    "UPDATE_MOVIE",
                    changes.toString()
            );
        }

        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = "movies", key = "#id")
    public void deleteMovie(Long id) {
        logger.info("Deleting movie with id: {}", id);

        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id);
        }

        movieRepository.deleteById(id);
        logger.info("Movie deleted successfully with id: {}", id);

        asyncNotificationService.logUserActionAsync(
                null,
                "DELETE_MOVIE",
                "Movie deleted with id: " + id
        );
    }

    @Override
    @Cacheable(value = "movies", key = "'list_' + #genre + '_' + #search + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MovieResponse> getMovies(String genre, String search, Pageable pageable) {
        logger.info("Fetching movies from DATABASE (not cache): genre={}, search={}", genre, search);

        Specification<Movie> spec = (root, query, cb) -> cb.conjunction();

        if (genre != null && !genre.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("genre"), genre));
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
        }

        return movieRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    // ================= MongoDB =================

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public void saveMovieToMongo(MovieCreateRequest request) {
        logger.info("Saving movie to MongoDB: {}", request.getTitle());

        MovieDocument doc = new MovieDocument();
        doc.setTitle(request.getTitle());
        doc.setGenre(request.getGenre());
        doc.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        movieMongoRepository.save(doc);

        asyncNotificationService.logUserActionAsync(
                null,
                "SAVE_TO_MONGO",
                "Movie saved to MongoDB: " + request.getTitle()
        );
    }

    @Override
    public Page<MovieResponse> getMoviesFromMongo(Pageable pageable) {
        logger.debug("Fetching movies from MongoDB");
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