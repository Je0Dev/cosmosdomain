package com.cosmosdomain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subtitles")
public class Subtitle {

    public enum Status {
        UPLOADED, CONVERTED, ERROR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_media_id")
    private LocalMedia localMedia;

    private String language;

    private String label;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "original_filename")
    private String originalFilename;

    @Enumerated(EnumType.STRING)
    private Status status = Status.UPLOADED;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalMedia getLocalMedia() { return localMedia; }
    public void setLocalMedia(LocalMedia localMedia) { this.localMedia = localMedia; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}