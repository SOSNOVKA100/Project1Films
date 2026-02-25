package com.example.project1films.service;

import com.example.project1films.entity.Movie;
import com.example.project1films.entity.Rental;
import com.example.project1films.entity.User;
import com.example.project1films.repository.MovieRepository;
import com.example.project1films.repository.RentalRepository;
import com.example.project1films.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public RentalServiceImpl(
            RentalRepository rentalRepository,
            UserRepository userRepository,
            MovieRepository movieRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    @Override
    public Rental getRentalById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));
    }

    @Override
    public Rental createRental(Long userId, Long movieId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found"));

        Rental rental = new Rental();

        rental.setUser(user);
        rental.setMovie(movie);
        rental.setRentDate(LocalDate.now());

        return rentalRepository.save(rental);
    }

    @Override
    public void deleteRental(Long id) {
        rentalRepository.deleteById(id);
    }
}
