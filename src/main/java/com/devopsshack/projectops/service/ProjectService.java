package com.devopsshack.projectops.service;

import com.devopsshack.projectops.model.*;
import com.devopsshack.projectops.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Project> search(String q, ProjectStatus status, ProjectCategory category,
                                ProjectPriority priority, Boolean favorite, String sortKey, int page) {
        Specification<Project> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            String pattern = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("summary")), pattern),
                    cb.like(cb.lower(root.get("owner")), pattern),
                    cb.like(cb.lower(root.get("tags")), pattern)
            ));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (priority != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
        }
        if (favorite != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("favorite"), favorite));
        }

        Sort sort = switch (sortKey == null ? "updated" : sortKey) {
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            case "progress" -> Sort.by(Sort.Direction.DESC, "progress");
            case "created" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "priority" -> Sort.by(Sort.Direction.DESC, "priority");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
        return repository.findAll(spec, PageRequest.of(Math.max(page, 0), 8, sort));
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    @Transactional(readOnly = true)
    public Project get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
    }

    public Project save(Project project) {
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            project.setProgress(100);
        }
        return repository.save(project);
    }

    public Project update(Long id, Project incoming) {
        Project existing = get(id);
        existing.setName(incoming.getName());
        existing.setSummary(incoming.getSummary());
        existing.setCategory(incoming.getCategory());
        existing.setStatus(incoming.getStatus());
        existing.setPriority(incoming.getPriority());
        existing.setEnvironment(incoming.getEnvironment());
        existing.setOwner(incoming.getOwner());
        existing.setProgress(incoming.getStatus() == ProjectStatus.COMPLETED ? 100 : incoming.getProgress());
        existing.setFavorite(incoming.isFavorite());
        existing.setRepoUrl(incoming.getRepoUrl());
        existing.setTags(incoming.getTags());
        existing.setStartDate(incoming.getStartDate());
        existing.setTargetDate(incoming.getTargetDate());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.delete(get(id));
    }

    public Project toggleFavorite(Long id) {
        Project project = get(id);
        project.setFavorite(!project.isFavorite());
        return repository.save(project);
    }

    public Project cycleStatus(Long id) {
        Project project = get(id);
        ProjectStatus next = switch (project.getStatus()) {
            case PLANNED -> ProjectStatus.ACTIVE;
            case ACTIVE -> ProjectStatus.BLOCKED;
            case BLOCKED -> ProjectStatus.COMPLETED;
            case COMPLETED -> ProjectStatus.ARCHIVED;
            case ARCHIVED -> ProjectStatus.PLANNED;
        };
        project.setStatus(next);
        if (next == ProjectStatus.COMPLETED) {
            project.setProgress(100);
        }
        return repository.save(project);
    }

    public Project archive(Long id) {
        Project project = get(id);
        project.setStatus(ProjectStatus.ARCHIVED);
        return repository.save(project);
    }

    public Project updateProgress(Long id, int progress) {
        Project project = get(id);
        int safeProgress = Math.max(0, Math.min(100, progress));
        project.setProgress(safeProgress);
        if (safeProgress == 100) {
            project.setStatus(ProjectStatus.COMPLETED);
        } else if (project.getStatus() == ProjectStatus.COMPLETED) {
            project.setStatus(ProjectStatus.ACTIVE);
        }
        return repository.save(project);
    }

    public Project duplicate(Long id) {
        Project source = get(id);
        Project copy = new Project(
                source.getName() + " - Copy",
                source.getSummary(),
                source.getCategory(),
                ProjectStatus.PLANNED,
                source.getPriority(),
                source.getEnvironment(),
                source.getOwner(),
                0,
                false,
                source.getRepoUrl(),
                source.getTags(),
                source.getStartDate(),
                source.getTargetDate()
        );
        return repository.save(copy);
    }

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        return new DashboardStats(
                repository.count(),
                repository.countByStatus(ProjectStatus.ACTIVE),
                repository.countByStatus(ProjectStatus.COMPLETED),
                repository.countByPriority(ProjectPriority.CRITICAL),
                repository.countByFavoriteTrue(),
                Math.round(repository.averageProgress())
        );
    }

    public record DashboardStats(long total, long active, long completed, long critical,
                                 long favorites, long averageProgress) {
    }
}
