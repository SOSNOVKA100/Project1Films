package com.example.project1films.service;

import com.example.project1films.dto.request.RentalCreateRequest;
import com.example.project1films.dto.response.RentalResponse;
import com.example.project1films.entity.Movie;
import com.example.project1films.entity.Rental;
import com.example.project1films.entity.User;
import com.example.project1films.exception.BusinessException;
import com.example.project1films.exception.ResourceNotFoundException;
import com.example.project1films.repository.MovieRepository;
import com.example.project1films.repository.RentalRepository;
import com.example.project1films.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalServiceImpl implements RentalService {

    private static final Logger logger = LoggerFactory.getLogger(RentalServiceImpl.class);

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final AsyncNotificationService asyncNotificationService;

    public RentalServiceImpl(RentalRepository rentalRepository,
                             UserRepository userRepository,
                             MovieRepository movieRepository,
                             AsyncNotificationService asyncNotificationService) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.asyncNotificationService = asyncNotificationService;
    }

    @Override
    @Transactional
    public RentalResponse createRental(RentalCreateRequest request) {
        logger.info("Creating rental for user: {}, movie: {}", request.getUserId(), request.getMovieId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));

        if (!movie.getAvailable()) {
            throw new BusinessException("Movie '" + movie.getTitle() + "' is not available for rent");
        }

        if (rentalRepository.existsByMovieIdAndUserId(movie.getId(), user.getId())) {
            throw new BusinessException("User already rented this movie");
        }

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setMovie(movie);

        movie.setAvailable(false);
        movieRepository.save(movie);

        Rental saved = rentalRepository.save(rental);
        logger.info("Rental created successfully with id: {}", saved.getId());

        RentalResponse response = mapToResponse(saved);

        asyncNotificationService.sendAccountUpdateNotificationAsync(
                mapUserToResponse(user),
                "Rented movie: " + movie.getTitle()
        );

        asyncNotificationService.logUserActionAsync(
                user.getId(),
                "CREATE_RENTAL",
                "Rented movie: " + movie.getTitle() + " (ID: " + movie.getId() + ")"
        );

        return response;
    }

    @Override
    public Page<RentalResponse> getRentals(Long userId, Long movieId, Pageable pageable) {
        logger.debug("Fetching rentals with userId: {}, movieId: {}", userId, movieId);

        Specification<Rental> spec = (root, query, cb) -> cb.conjunction();

        if (userId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("user").get("id"), userId));
        }

        if (movieId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("movie").get("id"), movieId));
        }

        return rentalRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public RentalResponse getRental(Long id) {
        logger.debug("Fetching rental with id: {}", id);

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + id));

        return mapToResponse(rental);
    }

    @Override
    @Transactional
    public void deleteRental(Long id) {
        logger.info("Deleting rental with id: {}", id);

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + id));

        Movie movie = rental.getMovie();
        movie.setAvailable(true);
        movieRepository.save(movie);

        rentalRepository.deleteById(id);
        logger.info("Rental deleted successfully with id: {}", id);

        asyncNotificationService.sendAccountUpdateNotificationAsync(
                mapUserToResponse(rental.getUser()),
                "Returned movie: " + movie.getTitle()
        );

        asyncNotificationService.logUserActionAsync(
                rental.getUser().getId(),
                "DELETE_RENTAL",
                "Returned movie: " + movie.getTitle()
        );
    }

    private RentalResponse mapToResponse(Rental rental) {
        RentalResponse response = new RentalResponse();
        response.setId(rental.getId());
        response.setUserId(rental.getUser().getId());
        response.setUserName(rental.getUser().getName());
        response.setMovieId(rental.getMovie().getId());
        response.setMovieTitle(rental.getMovie().getTitle());
        response.setRentDate(rental.getRentDate());
        return response;
    }

    private com.example.project1films.dto.response.UserResponse mapUserToResponse(User user) {
        com.example.project1films.dto.response.UserResponse response =
                new com.example.project1films.dto.response.UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}