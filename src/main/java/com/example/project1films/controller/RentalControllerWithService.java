package com.example.project1films.controller;

import com.example.project1films.dto.request.RentalCreateRequest;
import com.example.project1films.dto.response.RentalResponse;
import com.example.project1films.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rentals")
public class RentalControllerWithService {

    private final RentalService rentalService;

    public RentalControllerWithService(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    public ResponseEntity<RentalResponse> createRental(@Valid @RequestBody RentalCreateRequest request) {
        RentalResponse response = rentalService.createRental(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<RentalResponse>> getRentals(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long movieId,
            Pageable pageable
    ) {
        Page<RentalResponse> rentals = rentalService.getRentals(userId, movieId, pageable);
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getRental(@PathVariable Long id) {
        RentalResponse response = rentalService.getRental(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
        return ResponseEntity.noContent().build();
    }
}