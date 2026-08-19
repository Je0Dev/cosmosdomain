package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbPersonDetail {

    private Long id;
    private String name;
    @JsonProperty("birth_date")
    private LocalDate birthDate;
    @JsonProperty("death_date")
    private LocalDate deathDate;
    private String biography;
    @JsonProperty("place_of_birth")
    private String placeOfBirth;
    private Integer gender;
    @JsonProperty("known_for_department")
    private String knownForDepartment;
    @JsonProperty("profile_path")
    private String profilePath;
    @JsonProperty("also_known_as")
    private List<String> alsoKnownAs;
    private Double popularity;
    @JsonProperty("known_for")
    private List<TmdbPersonResult> knownFor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDate getDeathDate() { return deathDate; }
    public void setDeathDate(LocalDate deathDate) { this.deathDate = deathDate; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public String getKnownForDepartment() { return knownForDepartment; }
    public void setKnownForDepartment(String knownForDepartment) { this.knownForDepartment = knownForDepartment; }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }

    public List<String> getAlsoKnownAs() { return alsoKnownAs; }
    public void setAlsoKnownAs(List<String> alsoKnownAs) { this.alsoKnownAs = alsoKnownAs; }

    public Double getPopularity() { return popularity; }
    public void setPopularity(Double popularity) { this.popularity = popularity; }

    public List<TmdbPersonResult> getKnownFor() { return knownFor; }
    public void setKnownFor(List<TmdbPersonResult> knownFor) { this.knownFor = knownFor; }

    public String getProfileImageUrl(String imageBaseUrl, String profileSize) {
        return profilePath != null ? imageBaseUrl + "/" + profileSize + profilePath : "/images/default-avatar.png";
    }
}