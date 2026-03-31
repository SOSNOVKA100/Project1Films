package com.example.project1films.dto.request;

import jakarta.validation.constraints.Size;

public class MovieUpdateRequest {

    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(max = 50, message = "Genre must be less than 50 characters")
    private String genre;

    private Boolean available;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}