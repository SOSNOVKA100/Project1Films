package com.example.project1films.specification;

import com.example.project1films.entity.Movie;
import org.springframework.data.jpa.domain.Specification;

public class MovieSpecification {

    public static Specification<Movie> hasGenre(String genre) {
        return (root, query, cb) ->
                genre == null || genre.isBlank()
                        ? null
                        : cb.equal(root.get("genre"), genre);
    }

    public static Specification<Movie> hasYear(Integer year) {
        return (root, query, cb) ->
                year == null
                        ? null
                        : cb.equal(root.get("year"), year);
    }

    public static Specification<Movie> titleContains(String keyword) {
        return (root, query, cb) ->
                keyword == null || keyword.isBlank()
                        ? null
                        : cb.like(
                        cb.lower(root.get("title")),
                        "%" + keyword.toLowerCase() + "%"
                );
    }

    // year
    public static Specification<Movie> yearBetween(Integer from, Integer to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null)
                return cb.between(root.get("year"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("year"), from);
            return cb.lessThanOrEqualTo(root.get("year"), to);
        };
    }
}