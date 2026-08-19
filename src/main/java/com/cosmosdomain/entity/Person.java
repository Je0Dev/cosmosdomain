package com.cosmosdomain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "people")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_id", unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String biography;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "known_for_department")
    private String knownForDepartment;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "gender")
    private Integer gender;

    @ElementCollection
    @CollectionTable(name = "person_known_for", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "title")
    private Set<String> knownForTitles = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTmdbId() { return tmdbId; }
    public void setTmdbId(Long tmdbId) { this.tmdbId = tmdbId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }

    public String getKnownForDepartment() { return knownForDepartment; }
    public void setKnownForDepartment(String knownForDepartment) { this.knownForDepartment = knownForDepartment; }

    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public Set<String> getKnownForTitles() { return knownForTitles; }
    public void setKnownForTitles(Set<String> knownForTitles) { this.knownForTitles = knownForTitles; }

    public void addKnownForTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.knownForTitles.add(title);
        }
    }

    public String getProfileImageUrl(String imageBaseUrl, String profileSize) {
        return profilePath != null ? imageBaseUrl + "/" + profileSize + profilePath : "/images/default-avatar.png";
    }
}