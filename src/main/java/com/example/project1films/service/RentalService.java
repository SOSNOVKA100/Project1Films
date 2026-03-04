package com.example.project1films.service;

import com.example.project1films.dto.request.RentalCreateRequest;
import com.example.project1films.dto.response.RentalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RentalService {

    RentalResponse createRental(RentalCreateRequest request);
    Page<RentalResponse> getRentals(
            Long userId,
            Long movieId,
            Pageable pageable
    );
    RentalResponse getRental(Long id);
    void deleteRental(Long id);
}