package com.example.project1films.service;

import com.example.project1films.entity.mongo.ErrorLog;
import com.example.project1films.repository.ErrorLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MongoLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(MongoLoggingService.class);
    private final ErrorLogRepository errorLogRepository;

    public MongoLoggingService(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @Async("taskExecutor")
    public void saveErrorLog(ErrorLog errorLog) {
        try {

            if (errorLog.getErrorId() == null) {
                errorLog.setErrorId(UUID.randomUUID().toString());
            }

            errorLogRepository.save(errorLog);
            logger.debug("Error log saved to MongoDB with ID: {}", errorLog.getErrorId());

        } catch (Exception e) {
            logger.error("Failed to save error log to MongoDB: {}", e.getMessage());
            logger.error("Original error: {}", errorLog.getMessage());
        }
    }

    @Async("taskExecutor")
    public void saveErrorLogWithRetry(ErrorLog errorLog, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                errorLogRepository.save(errorLog);
                logger.debug("Error log saved to MongoDB on attempt {}", attempt);
                return;
            } catch (Exception e) {
                logger.warn("Failed to save error log (attempt {}/{}): {}",
                        attempt, maxRetries, e.getMessage());

                if (attempt == maxRetries) {
                    logger.error("Failed to save error log after {} attempts", maxRetries);
                    logger.error("ERROR LOG FALLBACK: {}", errorLog.getMessage());
                }

                try {
                    Thread.sleep(100 * attempt); // Экспоненциальная задержка
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}