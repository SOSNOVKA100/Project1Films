package com.example.project1films.repository;

import com.example.project1films.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByName(String name);
    boolean existsByName(String name);
    List<Author> findByRoleType(String roleType);
    List<Author> findByNationality(String nationality);

    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.movies WHERE a.id = :id")
    Optional<Author> findByIdWithMovies(@Param("id") Long id);
}