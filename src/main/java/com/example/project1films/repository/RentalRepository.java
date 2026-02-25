package com.example.project1films.repository;

import com.example.project1films.entity.Rental;
import com.example.project1films.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long>
{

}
