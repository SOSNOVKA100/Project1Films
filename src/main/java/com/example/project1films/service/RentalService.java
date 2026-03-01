package com.example.project1films.service;

import com.example.project1films.dto.request.RentalCreateRequest;
import com.example.project1films.dto.response.RentalResponse;
import java.util.List;

public interface RentalService {

    RentalResponse createRental(RentalCreateRequest request);
    List<RentalResponse> getAllRentals();
    RentalResponse getRental(Long id);
    void deleteRental(Long id);
}