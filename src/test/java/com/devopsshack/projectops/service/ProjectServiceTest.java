package com.devopsshack.projectops.service;

import com.devopsshack.projectops.model.*;
import com.devopsshack.projectops.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository repository;

    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(repository);
    }

    @Test
    void searchAppliesFiltersSortAndNormalizesNegativePage() {
        Project project = sampleProject();
        Page<Project> expected = new PageImpl<>(List.of(project));
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expected);

        Page<Project> result = service.search(
                " pipeline ",
                ProjectStatus.ACTIVE,
                ProjectCategory.CICD,
                ProjectPriority.HIGH,
                true,
                "name",
                -5
        );

        assertSame(expected, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(8, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("name").getDirection());
    }

    @Test
    void searchSupportsAllSortOptionsAndDefault() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertSort("progress", "progress", Sort.Direction.DESC);
        assertSort("created", "createdAt", Sort.Direction.DESC);
        assertSort("priority", "priority", Sort.Direction.DESC);
        assertSort("unknown", "updatedAt", Sort.Direction.DESC);
        assertSort(null, "updatedAt", Sort.Direction.DESC);
    }

    private void assertSort(String sortKey, String property, Sort.Direction direction) {
        service.search(null, null, null, null, null, sortKey, 0);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository, atLeastOnce()).findAll(any(Specification.class), captor.capture());
        Pageable last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(direction, last.getSort().getOrderFor(property).getDirection());
        clearInvocations(repository);
    }

    @Test
    void findAllUsesUpdatedAtDescending() {
        Project project = sampleProject();
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(project));

        List<Project> result = service.findAll();

        assertEquals(List.of(project), result);
        ArgumentCaptor<Sort> captor = ArgumentCaptor.forClass(Sort.class);
        verify(repository).findAll(captor.capture());
        assertEquals(Sort.Direction.DESC, captor.getValue().getOrderFor("updatedAt").getDirection());
    }

    @Test
    void getReturnsProjectOrThrows() {
        Project project = sampleProject();
        when(repository.findById(1L)).thenReturn(Optional.of(project));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertSame(project, service.get(1L));
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.get(99L));
        assertEquals("Project not found: 99", ex.getMessage());
    }

    @Test
    void saveForcesCompletedProjectToOneHundredPercent() {
        Project project = sampleProject();
        project.setStatus(ProjectStatus.COMPLETED);
        project.setProgress(25);
        when(repository.save(project)).thenReturn(project);

        Project result = service.save(project);

        assertEquals(100, result.getProgress());
        verify(repository).save(project);
    }

    @Test
    void saveKeepsProgressForNonCompletedProject() {
        Project project = sampleProject();
        project.setStatus(ProjectStatus.ACTIVE);
        project.setProgress(35);
        when(repository.save(project)).thenReturn(project);

        assertEquals(35, service.save(project).getProgress());
    }

    @Test
    void updateCopiesEveryEditableField() {
        Project existing = sampleProject();
        Project incoming = new Project(
                "Updated",
                "Updated summary",
                ProjectCategory.KUBERNETES,
                ProjectStatus.ACTIVE,
                ProjectPriority.CRITICAL,
                ProjectEnvironment.PROD,
                "Platform Team",
                73,
                true,
                "https://example.com/repo",
                "k8s,prod",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)
        );
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Project result = service.update(1L, incoming);

        assertEquals("Updated", result.getName());
        assertEquals("Updated summary", result.getSummary());
        assertEquals(ProjectCategory.KUBERNETES, result.getCategory());
        assertEquals(ProjectStatus.ACTIVE, result.getStatus());
        assertEquals(ProjectPriority.CRITICAL, result.getPriority());
        assertEquals(ProjectEnvironment.PROD, result.getEnvironment());
        assertEquals("Platform Team", result.getOwner());
        assertEquals(73, result.getProgress());
        assertTrue(result.isFavorite());
        assertEquals("https://example.com/repo", result.getRepoUrl());
        assertEquals("k8s,prod", result.getTags());
        assertEquals(LocalDate.of(2026, 1, 1), result.getStartDate());
        assertEquals(LocalDate.of(2026, 2, 1), result.getTargetDate());
    }

    @Test
    void updateForcesCompletedProjectToOneHundredPercent() {
        Project existing = sampleProject();
        Project incoming = sampleProject();
        incoming.setStatus(ProjectStatus.COMPLETED);
        incoming.setProgress(40);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertEquals(100, service.update(1L, incoming).getProgress());
    }

    @Test
    void deleteLoadsThenDeletesProject() {
        Project project = sampleProject();
        when(repository.findById(1L)).thenReturn(Optional.of(project));

        service.delete(1L);

        verify(repository).delete(project);
    }

    @Test
    void toggleFavoriteFlipsValue() {
        Project project = sampleProject();
        project.setFavorite(false);
        when(repository.findById(1L)).thenReturn(Optional.of(project));
        when(repository.save(project)).thenReturn(project);

        assertTrue(service.toggleFavorite(1L).isFavorite());
        assertFalse(service.toggleFavorite(1L).isFavorite());
    }

    @Test
    void cycleStatusCoversEntireLifecycle() {
        Project project = sampleProject();
        when(repository.findById(1L)).thenReturn(Optional.of(project));
        when(repository.save(project)).thenReturn(project);

        project.setStatus(ProjectStatus.PLANNED);
        assertEquals(ProjectStatus.ACTIVE, service.cycleStatus(1L).getStatus());
        assertEquals(ProjectStatus.BLOCKED, service.cycleStatus(1L).getStatus());
        assertEquals(ProjectStatus.COMPLETED, service.cycleStatus(1L).getStatus());
        assertEquals(100, project.getProgress());
        assertEquals(ProjectStatus.ARCHIVED, service.cycleStatus(1L).getStatus());
        assertEquals(ProjectStatus.PLANNED, service.cycleStatus(1L).getStatus());
    }

    @Test
    void archiveSetsArchivedStatus() {
        Project project = sampleProject();
        when(repository.findById(1L)).thenReturn(Optional.of(project));
        when(repository.save(project)).thenReturn(project);

        assertEquals(ProjectStatus.ARCHIVED, service.archive(1L).getStatus());
    }

    @Test
    void updateProgressClampsValuesAndSynchronizesStatus() {
        Project project = sampleProject();
        when(repository.findById(1L)).thenReturn(Optional.of(project));
        when(repository.save(project)).thenReturn(project);

        project.setStatus(ProjectStatus.ACTIVE);
        assertEquals(0, service.updateProgress(1L, -10).getProgress());

        assertEquals(100, service.updateProgress(1L, 150).getProgress());
        assertEquals(ProjectStatus.COMPLETED, project.getStatus());

        assertEquals(60, service.updateProgress(1L, 60).getProgress());
        assertEquals(ProjectStatus.ACTIVE, project.getStatus());
    }

    @Test
    void duplicateCreatesFreshPlannedCopy() {
        Project source = sampleProject();
        source.setFavorite(true);
        source.setProgress(90);
        when(repository.findById(1L)).thenReturn(Optional.of(source));
        when(repository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project copy = service.duplicate(1L);

        assertEquals(source.getName() + " - Copy", copy.getName());
        assertEquals(source.getSummary(), copy.getSummary());
        assertEquals(ProjectStatus.PLANNED, copy.getStatus());
        assertEquals(0, copy.getProgress());
        assertFalse(copy.isFavorite());
        assertEquals(source.getCategory(), copy.getCategory());
        assertEquals(source.getPriority(), copy.getPriority());
        assertEquals(source.getEnvironment(), copy.getEnvironment());
        assertEquals(source.getOwner(), copy.getOwner());
        assertEquals(source.getRepoUrl(), copy.getRepoUrl());
        assertEquals(source.getTags(), copy.getTags());
    }

    @Test
    void statsAggregatesRepositoryValues() {
        when(repository.count()).thenReturn(10L);
        when(repository.countByStatus(ProjectStatus.ACTIVE)).thenReturn(4L);
        when(repository.countByStatus(ProjectStatus.COMPLETED)).thenReturn(3L);
        when(repository.countByPriority(ProjectPriority.CRITICAL)).thenReturn(2L);
        when(repository.countByFavoriteTrue()).thenReturn(5L);
        when(repository.averageProgress()).thenReturn(64.6);

        ProjectService.DashboardStats stats = service.stats();

        assertEquals(10, stats.total());
        assertEquals(4, stats.active());
        assertEquals(3, stats.completed());
        assertEquals(2, stats.critical());
        assertEquals(5, stats.favorites());
        assertEquals(65, stats.averageProgress());
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
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );
        project.setId(1L);
        return project;
    }
}
