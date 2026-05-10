package com.example.project1films.service;

import com.example.project1films.dto.request.AuthorRequest;
import com.example.project1films.dto.response.AuthorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorService {
    AuthorResponse createAuthor(AuthorRequest request);
    AuthorResponse getAuthorById(Long id);
    Page<AuthorResponse> getAllAuthors(Pageable pageable);
    AuthorResponse updateAuthor(Long id, AuthorRequest request);
    void deleteAuthor(Long id);
}