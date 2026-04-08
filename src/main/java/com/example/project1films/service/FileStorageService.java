package com.example.project1films.service;

import com.example.project1films.dto.response.FileResponse;
import com.example.project1films.entity.mongo.FileMetadata;
import com.example.project1films.exception.FileStorageException;
import com.example.project1films.repository.mongo.FileMetadataRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;
    private final FileMetadataRepository fileMetadataRepository;
    private final AsyncNotificationService asyncNotificationService;

    @Value("${file.max-size:10485760}")
    private long maxFileSize;

    @Value("${file.allowed-types:image/jpeg,image/png,image/jpg}")
    private List<String> allowedContentTypes;

    public FileStorageService(GridFsTemplate gridFsTemplate,
                              GridFsOperations gridFsOperations,
                              FileMetadataRepository fileMetadataRepository,
                              AsyncNotificationService asyncNotificationService) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
        this.fileMetadataRepository = fileMetadataRepository;
        this.asyncNotificationService = asyncNotificationService;
    }

    // ================= UPLOAD =================

    public FileResponse uploadFile(MultipartFile file, String category,
                                   String entityId, String description,
                                   String uploadedBy) {

        validateFile(file);

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("category", category);
            metadata.put("entityId", entityId != null ? entityId : "");
            metadata.put("uploadedBy", uploadedBy);
            metadata.put("originalName", file.getOriginalFilename());
            metadata.put("description", description != null ? description : "");

            // save to GridFS
            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    metadata
            );

            // save metadata
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileId(fileId.toString());
            fileMetadata.setOriginalFileName(file.getOriginalFilename());
            fileMetadata.setStoredFileName(fileId.toString());
            fileMetadata.setContentType(file.getContentType());
            fileMetadata.setFileSize(file.getSize());
            fileMetadata.setCategory(category);
            fileMetadata.setEntityId(entityId);
            fileMetadata.setDescription(description);
            fileMetadata.setUploadedBy(uploadedBy);
            fileMetadata.setUploadDate(LocalDateTime.now());

            fileMetadataRepository.save(fileMetadata);

            logger.info("File uploaded successfully: {} with ID: {}",
                    file.getOriginalFilename(), fileId);

            // Асинхронное логирование
            asyncNotificationService.logUserActionAsync(
                    null, "FILE_UPLOAD",
                    String.format("File uploaded: %s, category: %s, size: %d bytes",
                            file.getOriginalFilename(), category, file.getSize())
            );

            return new FileResponse(
                    fileId.toString(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    category,
                    entityId
            );

        } catch (IOException e) {
            logger.error("Failed to upload file: {}", e.getMessage());
            throw new FileStorageException("Failed to upload file: " + e.getMessage());
        }
    }

    // ================= DOWNLOAD =================

    public GridFsResource downloadFile(String fileId) {
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    Query.query(Criteria.where("_id").is(new ObjectId(fileId)))
            );

            if (gridFSFile == null) {
                throw new FileStorageException("File not found with ID: " + fileId);
            }

            return gridFsOperations.getResource(gridFSFile);

        } catch (Exception e) {
            logger.error("Failed to download file: {}", e.getMessage());
            throw new FileStorageException("Failed to download file: " + e.getMessage());
        }
    }

    // ================= DELETE =================

    public void deleteFile(String fileId) {
        try {
            // delete from GridFS
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(new ObjectId(fileId))));

            // delete detadata
            fileMetadataRepository.deleteByFileId(fileId);

            logger.info("File deleted successfully: {}", fileId);

            // asyn logging
            asyncNotificationService.logUserActionAsync(
                    null, "FILE_DELETE",
                    "File deleted: " + fileId
            );

        } catch (Exception e) {
            logger.error("Failed to delete file: {}", e.getMessage());
            throw new FileStorageException("Failed to delete file: " + e.getMessage());
        }
    }

    // ================= GET FILE INFO =================

    public Map<String, Object> getFileInfo(String fileId) {
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    Query.query(Criteria.where("_id").is(new ObjectId(fileId)))
            );

            if (gridFSFile == null) {
                throw new FileStorageException("File not found with ID: " + fileId);
            }

            Optional<FileMetadata> metadata = fileMetadataRepository.findByFileId(fileId);

            Map<String, Object> info = new HashMap<>();
            info.put("fileId", fileId);
            info.put("filename", gridFSFile.getFilename());
            info.put("contentType", gridFSFile.getMetadata().get("_contentType"));
            info.put("size", gridFSFile.getLength());
            info.put("uploadDate", gridFSFile.getUploadDate());

            if (metadata.isPresent()) {
                info.put("category", metadata.get().getCategory());
                info.put("entityId", metadata.get().getEntityId());
                info.put("uploadedBy", metadata.get().getUploadedBy());
                info.put("description", metadata.get().getDescription());
            }

            return info;

        } catch (Exception e) {
            logger.error("Failed to get file info: {}", e.getMessage());
            throw new FileStorageException("Failed to get file info: " + e.getMessage());
        }
    }

    // ================= LIST FILES =================

    public List<FileResponse> getFilesByEntity(String entityId) {
        List<FileMetadata> metadataList = fileMetadataRepository.findByEntityId(entityId);

        return metadataList.stream()
                .map(meta -> new FileResponse(
                        meta.getFileId(),
                        meta.getOriginalFileName(),
                        meta.getContentType(),
                        meta.getFileSize(),
                        meta.getCategory(),
                        meta.getEntityId()
                ))
                .toList();
    }

    // ================= VALIDATION =================

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileStorageException(
                    String.format("File size exceeds maximum allowed: %d bytes (max: %d)",
                            file.getSize(), maxFileSize)
            );
        }

        String contentType = file.getContentType();
        if (!allowedContentTypes.contains(contentType)) {
            throw new FileStorageException(
                    String.format("File type %s is not allowed. Allowed types: %s",
                            contentType, allowedContentTypes)
            );
        }
    }
}