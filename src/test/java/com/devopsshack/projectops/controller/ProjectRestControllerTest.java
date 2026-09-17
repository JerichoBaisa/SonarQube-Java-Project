package com.devopsshack.projectops.controller;

import com.devopsshack.projectops.model.Project;
import com.devopsshack.projectops.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectRestControllerTest {

    @Mock
    private ProjectService service;

    private ProjectRestController controller;

    @BeforeEach
    void setUp() {
        controller = new ProjectRestController(service);
    }

    @Test
    void delegatesAllRestOperationsToService() {
        Project project = new Project();
        project.setId(1L);
        Project created = new Project();
        created.setId(2L);

        when(service.findAll()).thenReturn(List.of(project));
        when(service.get(1L)).thenReturn(project);
        when(service.save(project)).thenReturn(created);
        when(service.update(1L, project)).thenReturn(project);
        when(service.toggleFavorite(1L)).thenReturn(project);
        when(service.updateProgress(1L, 75)).thenReturn(project);

        assertEquals(List.of(project), controller.all());
        assertSame(project, controller.one(1L));

        project.setId(999L);
        assertSame(created, controller.create(project));
        assertNull(project.getId());

        assertSame(project, controller.update(1L, project));
        controller.delete(1L);
        assertSame(project, controller.favorite(1L));
        assertSame(project, controller.progress(1L, 75));

        verify(service).delete(1L);
    }

    @Test
    void apiExceptionHandlerBuilds404Response() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        var response = handler.notFound(new EntityNotFoundException("Project not found: 99"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("Project not found: 99", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }
}
