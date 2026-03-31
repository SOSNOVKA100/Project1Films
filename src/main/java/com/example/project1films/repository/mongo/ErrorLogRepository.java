package com.example.project1films.repository;

import com.example.project1films.entity.mongo.ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErrorLogRepository extends MongoRepository<ErrorLog, String> {

    // time error
    Page<ErrorLog> findByErrorType(String errorType, Pageable pageable);

    // search timemark
    List<ErrorLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Search status
    Page<ErrorLog> findByStatus(int status, Pageable pageable);

    // Search path
    Page<ErrorLog> findByPathContaining(String path, Pageable pageable);

    // Search IP client
    Page<ErrorLog> findByClientIp(String clientIp, Pageable pageable);

    //  количество ошибок по типам за последние 24 часа
    @Query(value = "{ 'timestamp': { $gte: ?0 } }",
            count = true,
            fields = "{ 'errorType': 1 }")
    long countByErrorTypeAfter(LocalDateTime timestamp);

    @Query("{ $and: [ " +
            "{ 'errorType': ?0 }, " +
            "{ 'status': ?1 }, " +
            "{ 'timestamp': { $gte: ?2 } } " +
            "] }")
    List<ErrorLog> findErrorsByTypeAndStatusAndAfterDate(
            String errorType,
            int status,
            LocalDateTime afterDate
    );
}