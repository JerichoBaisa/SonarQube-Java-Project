package com.devopsshack.projectops.controller;

import com.devopsshack.projectops.model.*;
import com.devopsshack.projectops.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Controller
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping({"/", "/projects"})
    public String dashboard(@RequestParam(required = false) String q,
                            @RequestParam(required = false) ProjectStatus status,
                            @RequestParam(required = false) ProjectCategory category,
                            @RequestParam(required = false) ProjectPriority priority,
                            @RequestParam(required = false) Boolean favorite,
                            @RequestParam(defaultValue = "updated") String sort,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Page<Project> projects = service.search(q, status, category, priority, favorite, sort, page);
        model.addAttribute("projects", projects);
        model.addAttribute("stats", service.stats());
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("favorite", favorite);
        model.addAttribute("sort", sort);
        addEnums(model);
        return "index";
    }

    @GetMapping("/projects/new")
    public String createForm(Model model) {
        Project project = new Project();
        project.setOwner("DevOps Shack");
        project.setProgress(10);
        model.addAttribute("project", project);
        model.addAttribute("editing", false);
        addEnums(model);
        return "project-form";
    }

    @PostMapping("/projects")
    public String create(@Valid @ModelAttribute("project") Project project,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("editing", false);
            addEnums(model);
            return "project-form";
        }
        Project saved = service.save(project);
        redirect.addFlashAttribute("success", "Project created successfully.");
        return "redirect:/projects/" + saved.getId();
    }

    @GetMapping("/projects/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("project", service.get(id));
        return "project-details";
    }

    @GetMapping("/projects/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", service.get(id));
        model.addAttribute("editing", true);
        addEnums(model);
        return "project-form";
    }

    @PostMapping("/projects/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("project") Project project,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            project.setId(id);
            model.addAttribute("editing", true);
            addEnums(model);
            return "project-form";
        }
        service.update(id, project);
        redirect.addFlashAttribute("success", "Project updated successfully.");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("success", "Project deleted.");
        return "redirect:/projects";
    }

    @PostMapping("/projects/{id}/favorite")
    public String favorite(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer,
                           RedirectAttributes redirect) {
        service.toggleFavorite(id);
        redirect.addFlashAttribute("success", "Favorite updated.");
        return redirectBack(referer);
    }

    @PostMapping("/projects/{id}/status")
    public String cycleStatus(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer,
                              RedirectAttributes redirect) {
        Project project = service.cycleStatus(id);
        redirect.addFlashAttribute("success", "Status changed to " + project.getStatus().getLabel() + ".");
        return redirectBack(referer);
    }

    @PostMapping("/projects/{id}/archive")
    public String archive(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String referer,
                          RedirectAttributes redirect) {
        service.archive(id);
        redirect.addFlashAttribute("success", "Project archived.");
        return redirectBack(referer);
    }

    @PostMapping("/projects/{id}/duplicate")
    public String duplicate(@PathVariable Long id, RedirectAttributes redirect) {
        Project copy = service.duplicate(id);
        redirect.addFlashAttribute("success", "Project duplicated as a new planned project.");
        return "redirect:/projects/" + copy.getId();
    }

    @PostMapping("/projects/{id}/progress")
    public String progress(@PathVariable Long id, @RequestParam int progress,
                           @RequestHeader(value = "Referer", required = false) String referer,
                           RedirectAttributes redirect) {
        service.updateProgress(id, progress);
        redirect.addFlashAttribute("success", "Progress updated to " + Math.max(0, Math.min(100, progress)) + "%.");
        return redirectBack(referer);
    }

    @GetMapping(value = "/projects/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv() {
        StringBuilder csv = new StringBuilder("ID,Name,Category,Status,Priority,Environment,Owner,Progress,Favorite,Repository,Tags,Updated At\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        service.findAll().forEach(p -> csv.append(p.getId()).append(',')
                .append(csv(p.getName())).append(',')
                .append(csv(p.getCategory().getLabel())).append(',')
                .append(csv(p.getStatus().getLabel())).append(',')
                .append(csv(p.getPriority().getLabel())).append(',')
                .append(csv(p.getEnvironment().getLabel())).append(',')
                .append(csv(p.getOwner())).append(',')
                .append(p.getProgress()).append(',')
                .append(p.isFavorite()).append(',')
                .append(csv(p.getRepoUrl())).append(',')
                .append(csv(p.getTags())).append(',')
                .append(csv(p.getUpdatedAt().format(formatter))).append('\n'));

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=devops-shack-projects.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public String notFound(EntityNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "404";
    }

    private String csv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String redirectBack(String referer) {
        if (referer != null && referer.contains("/projects/")) {
            String path = referer.substring(referer.indexOf("/projects/"));
            return "redirect:" + path;
        }
        return "redirect:/projects";
    }

    private void addEnums(Model model) {
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("priorities", ProjectPriority.values());
        model.addAttribute("categories", ProjectCategory.values());
        model.addAttribute("environments", ProjectEnvironment.values());
    }
}
