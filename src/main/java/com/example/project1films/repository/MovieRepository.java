package com.example.project1films.repository;

import com.example.project1films.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>,
        JpaSpecificationExecutor<Movie> {

    Optional<Movie> findByTitle(String title);

    boolean existsByTitle(String title);
}