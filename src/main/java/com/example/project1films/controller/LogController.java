package com.example.project1films.controller;

import com.example.project1films.entity.mongo.ErrorLog;
import com.example.project1films.repository.ErrorLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    private final ErrorLogRepository errorLogRepository;

    public LogController(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @GetMapping
    public ResponseEntity<Page<ErrorLog>> getAllLogs(
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(errorLogRepository.findAll(pageable));
    }

    @GetMapping("/error-type/{errorType}")
    public ResponseEntity<Page<ErrorLog>> getLogsByErrorType(
            @PathVariable String errorType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(errorLogRepository.findByErrorType(errorType, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ErrorLog>> getLogsByStatus(
            @PathVariable int status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(errorLogRepository.findByStatus(status, pageable));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ErrorLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(errorLogRepository.findByTimestampBetween(start, end));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalErrors", errorLogRepository.count());

        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        stats.put("errorsLast24h", errorLogRepository.countByErrorTypeAfter(last24h));

        Map<Integer, Long> errorsByStatus = new HashMap<>();
        errorsByStatus.put(400, errorLogRepository.findByStatus(400, Pageable.unpaged()).getTotalElements());
        errorsByStatus.put(404, errorLogRepository.findByStatus(404, Pageable.unpaged()).getTotalElements());
        errorsByStatus.put(409, errorLogRepository.findByStatus(409, Pageable.unpaged()).getTotalElements());
        errorsByStatus.put(500, errorLogRepository.findByStatus(500, Pageable.unpaged()).getTotalElements());
        stats.put("errorsByStatus", errorsByStatus);

        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupOldLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime olderThan) {

        List<ErrorLog> oldLogs = errorLogRepository.findByTimestampBetween(
                LocalDateTime.MIN, olderThan);

        errorLogRepository.deleteAll(oldLogs);

        return ResponseEntity.ok(String.format("Deleted %d old logs", oldLogs.size()));
    }
}