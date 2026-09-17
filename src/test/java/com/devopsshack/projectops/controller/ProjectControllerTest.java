package com.devopsshack.projectops.controller;

import com.devopsshack.projectops.model.*;
import com.devopsshack.projectops.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService service;

    private ProjectController controller;

    @BeforeEach
    void setUp() {
        controller = new ProjectController(service);
    }

    @Test
    void dashboardPopulatesModelAndReturnsIndex() {
        Project project = sampleProject();
        var page = new PageImpl<>(List.of(project));
        var stats = new ProjectService.DashboardStats(1, 1, 0, 0, 0, 50);
        when(service.search("ci", ProjectStatus.ACTIVE, ProjectCategory.CICD,
                ProjectPriority.HIGH, true, "name", 0)).thenReturn(page);
        when(service.stats()).thenReturn(stats);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.dashboard("ci", ProjectStatus.ACTIVE, ProjectCategory.CICD,
                ProjectPriority.HIGH, true, "name", 0, model);

        assertEquals("index", view);
        assertSame(page, model.getAttribute("projects"));
        assertSame(stats, model.getAttribute("stats"));
        assertArrayEquals(ProjectStatus.values(), (ProjectStatus[]) model.getAttribute("statuses"));
        assertArrayEquals(ProjectPriority.values(), (ProjectPriority[]) model.getAttribute("priorities"));
        assertArrayEquals(ProjectCategory.values(), (ProjectCategory[]) model.getAttribute("categories"));
        assertArrayEquals(ProjectEnvironment.values(), (ProjectEnvironment[]) model.getAttribute("environments"));
    }

    @Test
    void createFormProvidesDefaults() {
        ConcurrentModel model = new ConcurrentModel();

        assertEquals("project-form", controller.createForm(model));
        Project project = (Project) model.getAttribute("project");
        assertNotNull(project);
        assertEquals("DevOps Shack", project.getOwner());
        assertEquals(10, project.getProgress());
        assertEquals(false, model.getAttribute("editing"));
    }

    @Test
    void createReturnsFormWhenValidationFails() {
        Project project = sampleProject();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);
        ConcurrentModel model = new ConcurrentModel();

        assertEquals("project-form", controller.create(project, result, model, new RedirectAttributesModelMap()));
        assertEquals(false, model.getAttribute("editing"));
        verifyNoInteractions(service);
    }

    @Test
    void createSavesAndRedirectsWhenValid() {
        Project project = sampleProject();
        project.setId(null);
        Project saved = sampleProject();
        saved.setId(10L);
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        when(service.save(project)).thenReturn(saved);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/projects/10", controller.create(project, result, new ConcurrentModel(), redirect));
        assertEquals("Project created successfully.", redirect.getFlashAttributes().get("success"));
    }

    @Test
    void detailsAndEditFormLoadProject() {
        Project project = sampleProject();
        when(service.get(1L)).thenReturn(project);

        ConcurrentModel detailsModel = new ConcurrentModel();
        assertEquals("project-details", controller.details(1L, detailsModel));
        assertSame(project, detailsModel.getAttribute("project"));

        ConcurrentModel editModel = new ConcurrentModel();
        assertEquals("project-form", controller.editForm(1L, editModel));
        assertSame(project, editModel.getAttribute("project"));
        assertEquals(true, editModel.getAttribute("editing"));
    }

    @Test
    void updateReturnsFormOnValidationErrorsAndPreservesId() {
        Project project = sampleProject();
        project.setId(null);
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);
        ConcurrentModel model = new ConcurrentModel();

        assertEquals("project-form", controller.update(7L, project, result, model, new RedirectAttributesModelMap()));
        assertEquals(7L, project.getId());
        assertEquals(true, model.getAttribute("editing"));
        verifyNoInteractions(service);
    }

    @Test
    void updateDelegatesAndRedirectsWhenValid() {
        Project project = sampleProject();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/projects/1", controller.update(1L, project, result, new ConcurrentModel(), redirect));
        verify(service).update(1L, project);
        assertEquals("Project updated successfully.", redirect.getFlashAttributes().get("success"));
    }

    @Test
    void actionEndpointsDelegateAndRedirect() {
        Project project = sampleProject();
        project.setStatus(ProjectStatus.BLOCKED);
        Project copy = sampleProject();
        copy.setId(22L);
        when(service.cycleStatus(1L)).thenReturn(project);
        when(service.duplicate(1L)).thenReturn(copy);

        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/projects", controller.delete(1L, redirect));
        verify(service).delete(1L);

        redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/projects/1", controller.favorite(1L, "http://localhost:8080/projects/1", redirect));
        verify(service).toggleFavorite(1L);

        redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/projects", controller.favorite(1L, null, redirect));

        redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/projects/1", controller.cycleStatus(1L, "http://localhost:8080/projects/1", redirect));
        assertEquals("Status changed to Blocked.", redirect.getFlashAttributes().get("success"));

        redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/projects/1", controller.archive(1L, "https://example.com/projects/1", redirect));
        verify(service).archive(1L);

        redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/projects/22", controller.duplicate(1L, redirect));

        redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/projects/1", controller.progress(1L, 140,
                "http://localhost:8080/projects/1", redirect));
        verify(service).updateProgress(1L, 140);
        assertEquals("Progress updated to 100%.", redirect.getFlashAttributes().get("success"));
    }

    @Test
    void exportCsvEscapesDataAndReturnsDownload() {
        Project project = sampleProject();
        project.setName("Demo, \"Quoted\"");
        project.setRepoUrl(null);
        project.setUpdatedAt(LocalDateTime.of(2026, 9, 16, 12, 30));
        when(service.findAll()).thenReturn(List.of(project));

        var response = controller.exportCsv();

        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
                .contains("devops-shack-projects.csv"));
        assertNotNull(response.getBody());
        String csv = new String(response.getBody(), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("ID,Name,Category"));
        assertTrue(csv.contains("\"Demo, \"\"Quoted\"\"\""));
        assertTrue(csv.contains("\"\""));
        assertTrue(csv.contains("2026-09-16 12:30"));
    }

    @Test
    void notFoundHandlerAddsMessageAndReturns404View() {
        ConcurrentModel model = new ConcurrentModel();
        String view = controller.notFound(new EntityNotFoundException("missing"), model);

        assertEquals("404", view);
        assertEquals("missing", model.getAttribute("message"));
    }

    private Project sampleProject() {
        Project project = new Project(
                "CI Pipeline",
                "Build and deploy",
                ProjectCategory.CICD,
                ProjectStatus.ACTIVE,
                ProjectPriority.HIGH,
                ProjectEnvironment.DEV,
                "DevOps Shack",
                50,
                false,
                "https://github.com/example/repo",
                "ci,java",
                null,
                null
        );
        project.setId(1L);
        project.setUpdatedAt(LocalDateTime.of(2026, 9, 16, 10, 0));
        return project;
    }
}
