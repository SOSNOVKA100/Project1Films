package com.example.project1films.controller;

import com.example.project1films.entity.Rental;
import com.example.project1films.repository.RentalRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/renta")
public class RentalController {

    private final RentalRepository repo;

    public RentalController(RentalRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Rental> getAllRentals() {
        return repo.findAll();
    }

    @PostMapping
    public Rental createRental(@RequestBody Rental rental) {
        return repo.save(rental);
    }
}
