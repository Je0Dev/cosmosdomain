package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TMDB person detail payload.
 *
 * <p>Represents the biography-style response used by the person profile page,
 * including birth/death dates, place of birth, aliases, and known-for titles.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbPersonDetail {

    // Identity and biography details
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

    // Profile image path (relative to the TMDB image CDN).
    @JsonProperty("profile_path")
    private String profilePath;

    // Alternate names and a short filmography preview.
    @JsonProperty("also_known_as")
    private List<String> alsoKnownAs;
    private Double popularity;
    @JsonProperty("known_for")
    private List<TmdbPersonResult> knownFor;
    @JsonProperty("combined_credits")
    private TmdbCombinedCredits combinedCredits;

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

    public TmdbCombinedCredits getCombinedCredits() { return combinedCredits; }
    public void setCombinedCredits(TmdbCombinedCredits combinedCredits) { this.combinedCredits = combinedCredits; }

    /**
     * Returns a short, poster-backed filmography for the "Known For" section.
     *
     * <p>Uses the explicit {@code known_for} list when TMDB provides one;
     * otherwise falls back to the person's combined cast credits, removes
     * duplicates, and keeps the most popular eight titles.</p>
     */
    public List<TmdbPersonResult> getKnownFor() {
        if (knownFor != null && !knownFor.isEmpty()) {
            return knownFor;
        }
        if (combinedCredits == null || combinedCredits.getCast() == null) {
            return List.of();
        }

        List<TmdbPersonResult> titles = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (TmdbPersonResult credit : combinedCredits.getCast()) {
            if (credit.getMediaType() == null || credit.getPosterPath() == null) {
                continue;
            }
            String key = credit.getMediaType() + ":" + credit.getId();
            if (seen.add(key)) {
                titles.add(credit);
            }
        }
        titles.sort(Comparator.comparing(
            TmdbPersonResult::getPopularity,
            Comparator.nullsLast(Comparator.reverseOrder())));
        return titles.stream().limit(8).toList();
    }

    public void setKnownFor(List<TmdbPersonResult> knownFor) { this.knownFor = knownFor; }
}