package com.example.project1films.service;

import com.example.project1films.dto.request.RentalCreateRequest;
import com.example.project1films.dto.response.RentalResponse;
import com.example.project1films.entity.Rental;
import com.example.project1films.entity.User;
import com.example.project1films.entity.Movie;
import com.example.project1films.repository.RentalRepository;
import com.example.project1films.repository.UserRepository;
import com.example.project1films.repository.MovieRepository;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public RentalServiceImpl(RentalRepository rentalRepository,
                             UserRepository userRepository,
                             MovieRepository movieRepository) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    public RentalResponse createRental(RentalCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setMovie(movie);

        Rental saved = rentalRepository.save(rental);
        return mapToResponse(saved);
    }

    @Override
    public List<RentalResponse> getAllRentals() {
        return rentalRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RentalResponse getRental(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        return mapToResponse(rental);
    }

    @Override
    public void deleteRental(Long id) {
        rentalRepository.deleteById(id);
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
}