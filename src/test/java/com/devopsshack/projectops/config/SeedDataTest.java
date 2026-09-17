package com.devopsshack.projectops.config;

import com.devopsshack.projectops.model.Project;
import com.devopsshack.projectops.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SeedDataTest {

    @Test
    void doesNothingWhenDatabaseAlreadyContainsProjects() throws Exception {
        ProjectRepository repository = mock(ProjectRepository.class);
        when(repository.count()).thenReturn(2L);

        CommandLineRunner runner = new SeedData().seedProjects(repository);
        runner.run();

        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void insertsSixProjectsWhenDatabaseIsEmpty() throws Exception {
        ProjectRepository repository = mock(ProjectRepository.class);
        when(repository.count()).thenReturn(0L);

        CommandLineRunner runner = new SeedData().seedProjects(repository);
        runner.run();

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertEquals(6, captor.getValue().size());
        Project first = (Project) captor.getValue().get(0);
        assertEquals("Production CI/CD Pipeline", first.getName());
    }
}
