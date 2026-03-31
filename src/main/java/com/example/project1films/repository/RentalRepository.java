package com.example.project1films.repository;

import com.example.project1films.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long>,
        JpaSpecificationExecutor<Rental> {

    List<Rental> findByUserId(Long userId);

    List<Rental> findByMovieId(Long movieId);

    boolean existsByMovieIdAndUserId(Long movieId, Long userId);
}