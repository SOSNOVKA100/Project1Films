package com.example.project1films.controller;

import com.example.project1films.entity.Rental;
import com.example.project1films.service.RentalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rentals/1")
public class RentalControllerWithService {

    private final RentalService rentalService;

    public RentalControllerWithService(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalService.getAllRentals();
    }

    @GetMapping("/{id}")
    public Rental getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }

    @PostMapping
    public Rental createRental(
            @RequestParam Long userId,
            @RequestParam Long movieId
    ) {
        return rentalService.createRental(userId, movieId);
    }

    @DeleteMapping("/{id}")
    public void deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
    }
}
