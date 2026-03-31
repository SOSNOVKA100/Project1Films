package com.example.project1films.repository;

import com.example.project1films.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MovieRepository extends
        JpaRepository<Movie, Long>,
        JpaSpecificationExecutor<Movie> {
}