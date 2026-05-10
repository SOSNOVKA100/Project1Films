package com.example.project1films.dto.response;

import java.time.LocalDate;

public class AuthorResponse {
    private Long id;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private String nationality;
    private String roleType;
    private Long moviesCount;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }

    public Long getMoviesCount() { return moviesCount; }
    public void setMoviesCount(Long moviesCount) { this.moviesCount = moviesCount; }
}