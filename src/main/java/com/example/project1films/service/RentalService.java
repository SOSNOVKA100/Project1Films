package com.example.project1films.service;

import com.example.project1films.entity.Rental;
import java.util.List;

public interface RentalService {

    List<Rental> getAllRentals();

    Rental getRentalById(Long id);

    Rental createRental(Long userId, Long movieId);

    void deleteRental(Long id);
}
