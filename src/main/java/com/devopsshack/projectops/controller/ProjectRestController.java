package com.devopsshack.projectops.controller;

import com.devopsshack.projectops.model.Project;
import com.devopsshack.projectops.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {

    private final ProjectService service;

    public ProjectRestController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<Project> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Project one(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Project create(@Valid @RequestBody Project project) {
        project.setId(null);
        return service.save(project);
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable Long id, @Valid @RequestBody Project project) {
        return service.update(id, project);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/favorite")
    public Project favorite(@PathVariable Long id) {
        return service.toggleFavorite(id);
    }

    @PatchMapping("/{id}/progress")
    public Project progress(@PathVariable Long id, @RequestParam int value) {
        return service.updateProgress(id, value);
    }
}
