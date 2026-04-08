package com.example.project1films.repository.mongo;

import com.example.project1films.entity.mongo.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {

    Optional<FileMetadata> findByFileId(String fileId);

    Page<FileMetadata> findByCategory(String category, Pageable pageable);

    List<FileMetadata> findByEntityId(String entityId);

    Optional<FileMetadata> findByCategoryAndEntityId(String category, String entityId);

    Page<FileMetadata> findByUploadedBy(String uploadedBy, Pageable pageable);

    void deleteByFileId(String fileId);

    List<FileMetadata> findByEntityIdAndIsActiveTrue(String entityId);

    // Сложный поиск
    @Query("{ $and: [ " +
            "{ 'category': ?0 }, " +
            "{ 'uploadedBy': ?1 }, " +
            "{ 'uploadDate': { $gte: ?2 } } " +
            "] }")
    List<FileMetadata> findFilesByCategoryAndUserAfterDate(
            String category, String uploadedBy, LocalDateTime afterDate
    );
}