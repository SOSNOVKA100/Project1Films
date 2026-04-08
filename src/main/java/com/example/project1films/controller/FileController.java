package com.example.project1films.controller;

import com.example.project1films.dto.response.FileResponse;
import com.example.project1films.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // ================= UPLOAD =================

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {

        String uploadedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        FileResponse response = fileStorageService.uploadFile(
                file, category, entityId, description, uploadedBy
        );

        return ResponseEntity.ok(response);
    }

    // ================= DOWNLOAD =================

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        GridFsResource resource = fileStorageService.downloadFile(fileId);

        String encodedFilename = URLEncoder.encode(
                resource.getFilename(),
                StandardCharsets.UTF_8
        ).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .body(resource);
    }

    // ================= GET INFO =================

    @GetMapping("/info/{fileId}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileId) {
        Map<String, Object> info = fileStorageService.getFileInfo(fileId);
        return ResponseEntity.ok(info);
    }

    // ================= LIST BY ENTITY =================

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<FileResponse>> getFilesByEntity(@PathVariable String entityId) {
        List<FileResponse> files = fileStorageService.getFilesByEntity(entityId);
        return ResponseEntity.ok(files);
    }

    // ================= DELETE =================

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        fileStorageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    // ================= PREVIEW (for picture) =================

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileId) {
        GridFsResource resource = fileStorageService.downloadFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .body(resource);
    }
}