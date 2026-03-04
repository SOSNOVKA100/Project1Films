package com.example.project1films.controller;

import com.example.project1films.dto.request.RentalCreateRequest;
import com.example.project1films.dto.response.RentalResponse;
import com.example.project1films.service.RentalService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rentals")
public class RentalControllerWithService {

    private final RentalService rentalService;

    public RentalControllerWithService(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    public RentalResponse createRental(@RequestBody RentalCreateRequest request) {
        return rentalService.createRental(request);
    }

    @GetMapping
    public Page<RentalResponse> getRentals(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long movieId,
            Pageable pageable
    ) {
        return rentalService.getRentals(userId, movieId, pageable);
    }

    @GetMapping("/{id}")
    public RentalResponse getRental(@PathVariable Long id) {
        return rentalService.getRental(id);
    }

    @DeleteMapping("/{id}")
    public void deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
    }
}