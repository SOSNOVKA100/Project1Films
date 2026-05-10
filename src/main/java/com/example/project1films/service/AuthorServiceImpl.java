package com.example.project1films.service;

import com.example.project1films.dto.request.AuthorRequest;
import com.example.project1films.dto.response.AuthorResponse;
import com.example.project1films.dto.response.AuthorResponse;
import com.example.project1films.entity.Author;
import com.example.project1films.exception.DuplicateResourceException;
import com.example.project1films.exception.ResourceNotFoundException;
import com.example.project1films.repository.AuthorRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AsyncNotificationService asyncNotificationService;

    public AuthorServiceImpl(AuthorRepository authorRepository,
                             AsyncNotificationService asyncNotificationService) {
        this.authorRepository = authorRepository;
        this.asyncNotificationService = asyncNotificationService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
    public AuthorResponse createAuthor(AuthorRequest request) {
        if (authorRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Author with name '" + request.getName() + "' already exists");
        }

        Author author = new Author();
        author.setName(request.getName());
        author.setBiography(request.getBiography());
        author.setBirthDate(request.getBirthDate());
        author.setNationality(request.getNationality());
        author.setRoleType(request.getRoleType());

        Author saved = authorRepository.save(author);

        asyncNotificationService.logUserActionAsync(
                null, "CREATE_AUTHOR", "Author created: " + saved.getName()
        );

        return convertToResponseDTO(saved);
    }

    @Override
    @Cacheable(value = "authors", key = "#id")
    public AuthorResponse getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        return convertToResponseDTO(author);
    }

    @Override
    public Page<AuthorResponse> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable).map(this::convertToResponseDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "authors", key = "#id")
    public AuthorResponse updateAuthor(Long id, AuthorRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(author.getName())) {
            if (authorRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Author with name '" + request.getName() + "' already exists");
            }
            author.setName(request.getName());
        }
        if (request.getBiography() != null) author.setBiography(request.getBiography());
        if (request.getBirthDate() != null) author.setBirthDate(request.getBirthDate());
        if (request.getNationality() != null) author.setNationality(request.getNationality());
        if (request.getRoleType() != null) author.setRoleType(request.getRoleType());

        Author updated = authorRepository.save(author);

        asyncNotificationService.logUserActionAsync(
                null, "UPDATE_AUTHOR", "Author updated: " + updated.getName()
        );

        return convertToResponseDTO(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "authors", key = "#id")
    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);

        asyncNotificationService.logUserActionAsync(
                null, "DELETE_AUTHOR", "Author deleted with id: " + id
        );
    }

    private AuthorResponse convertToResponseDTO(Author author) {
        AuthorResponse dto = new AuthorResponse();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setBiography(author.getBiography());
        dto.setBirthDate(author.getBirthDate());
        dto.setNationality(author.getNationality());
        dto.setRoleType(author.getRoleType());
        dto.setMoviesCount((long) author.getMovies().size());
        return dto;
    }
}