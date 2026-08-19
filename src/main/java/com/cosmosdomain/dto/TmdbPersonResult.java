package com.cosmosdomain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbPersonResult {

    private Long id;
    private Double popularity;
    @JsonProperty("name")
    private String name;
    @JsonProperty("title")
    private String title;
    @JsonProperty("character")
    private String character;
    @JsonProperty("job")
    private String job;
    @JsonProperty("department")
    private String department;
    @JsonProperty("known_for_department")
    private String knownForDepartment;
    @JsonProperty("profile_path")
    private String profilePath;
    @JsonProperty("poster_path")
    private String posterPath;
    private String mediaType;
    private String overview;
    @JsonProperty("release_date")
    private String releaseDate;
    @JsonProperty("first_air_date")
    private String firstAirDate;
    private Integer gender;
    @JsonProperty("origin_country")
    private List<String> originCountry;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getPopularity() { return popularity; }
    public void setPopularity(Double popularity) { this.popularity = popularity; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCharacter() { return character; }
    public void setCharacter(String character) { this.character = character; }

    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getKnownForDepartment() { return knownForDepartment; }
    public void setKnownForDepartment(String knownForDepartment) { this.knownForDepartment = knownForDepartment; }

    public String getProfilePath() { return profilePath; }
    public void setProfilePath(String profilePath) { this.profilePath = profilePath; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getFirstAirDate() { return firstAirDate; }
    public void setFirstAirDate(String firstAirDate) { this.firstAirDate = firstAirDate; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public List<String> getOriginCountry() { return originCountry; }
    public void setOriginCountry(List<String> originCountry) { this.originCountry = originCountry; }

    public String getDisplayName() {
        return name != null ? name : title;
    }
}