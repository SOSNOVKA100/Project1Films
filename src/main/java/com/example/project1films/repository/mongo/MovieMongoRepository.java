package com.example.project1films.repository.mongo;

import com.example.project1films.entity.mongo.MovieDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovieMongoRepository extends MongoRepository<MovieDocument, String> {
}