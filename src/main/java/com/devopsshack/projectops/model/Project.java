package com.devopsshack.projectops.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(max = 120, message = "Project name must be under 120 characters")
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank(message = "Summary is required")
    @Size(max = 600, message = "Summary must be under 600 characters")
    @Column(nullable = false, length = 600)
    private String summary;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectCategory category = ProjectCategory.CICD;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANNED;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @NotNull(message = "Environment is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectEnvironment environment = ProjectEnvironment.DEV;

    @NotBlank(message = "Owner is required")
    @Size(max = 80, message = "Owner must be under 80 characters")
    @Column(nullable = false, length = 80)
    private String owner;

    @Min(value = 0, message = "Progress cannot be below 0")
    @Max(value = 100, message = "Progress cannot exceed 100")
    @Column(nullable = false)
    private int progress = 0;

    @Column(nullable = false)
    private boolean favorite = false;

    @Size(max = 250, message = "Repository URL must be under 250 characters")
    @Column(length = 250)
    private String repoUrl;

    @Size(max = 180, message = "Tags must be under 180 characters")
    @Column(length = 180)
    private String tags;

    private LocalDate startDate;
    private LocalDate targetDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Project() {
    }

    public Project(String name, String summary, ProjectCategory category, ProjectStatus status,
                   ProjectPriority priority, ProjectEnvironment environment, String owner,
                   int progress, boolean favorite, String repoUrl, String tags,
                   LocalDate startDate, LocalDate targetDate) {
        this.name = name;
        this.summary = summary;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.environment = environment;
        this.owner = owner;
        this.progress = progress;
        this.favorite = favorite;
        this.repoUrl = repoUrl;
        this.tags = tags;
        this.startDate = startDate;
        this.targetDate = targetDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public ProjectCategory getCategory() { return category; }
    public void setCategory(ProjectCategory category) { this.category = category; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public ProjectPriority getPriority() { return priority; }
    public void setPriority(ProjectPriority priority) { this.priority = priority; }
    public ProjectEnvironment getEnvironment() { return environment; }
    public void setEnvironment(ProjectEnvironment environment) { this.environment = environment; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
