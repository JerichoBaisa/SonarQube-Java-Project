package com.devopsshack.projectops.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void defaultConstructorProvidesExpectedDefaults() {
        Project project = new Project();

        assertEquals(ProjectCategory.CICD, project.getCategory());
        assertEquals(ProjectStatus.PLANNED, project.getStatus());
        assertEquals(ProjectPriority.MEDIUM, project.getPriority());
        assertEquals(ProjectEnvironment.DEV, project.getEnvironment());
        assertEquals(0, project.getProgress());
        assertFalse(project.isFavorite());
    }

    @Test
    void constructorAndAccessorsWork() {
        LocalDate start = LocalDate.of(2026, 1, 10);
        LocalDate target = LocalDate.of(2026, 2, 10);
        Project project = new Project(
                "Project",
                "Summary",
                ProjectCategory.CLOUD,
                ProjectStatus.BLOCKED,
                ProjectPriority.CRITICAL,
                ProjectEnvironment.QA,
                "Owner",
                45,
                true,
                "https://example.com",
                "aws,java",
                start,
                target
        );

        project.setId(42L);
        project.setName("New Name");
        project.setSummary("New Summary");
        project.setCategory(ProjectCategory.PLATFORM);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setPriority(ProjectPriority.LOW);
        project.setEnvironment(ProjectEnvironment.MULTI);
        project.setOwner("New Owner");
        project.setProgress(66);
        project.setFavorite(false);
        project.setRepoUrl("https://github.com/devopsshack");
        project.setTags("platform,devops");
        project.setStartDate(start.plusDays(1));
        project.setTargetDate(target.plusDays(1));
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime updated = LocalDateTime.of(2026, 1, 2, 10, 0);
        project.setCreatedAt(created);
        project.setUpdatedAt(updated);

        assertEquals(42L, project.getId());
        assertEquals("New Name", project.getName());
        assertEquals("New Summary", project.getSummary());
        assertEquals(ProjectCategory.PLATFORM, project.getCategory());
        assertEquals(ProjectStatus.ACTIVE, project.getStatus());
        assertEquals(ProjectPriority.LOW, project.getPriority());
        assertEquals(ProjectEnvironment.MULTI, project.getEnvironment());
        assertEquals("New Owner", project.getOwner());
        assertEquals(66, project.getProgress());
        assertFalse(project.isFavorite());
        assertEquals("https://github.com/devopsshack", project.getRepoUrl());
        assertEquals("platform,devops", project.getTags());
        assertEquals(start.plusDays(1), project.getStartDate());
        assertEquals(target.plusDays(1), project.getTargetDate());
        assertEquals(created, project.getCreatedAt());
        assertEquals(updated, project.getUpdatedAt());
    }

    @Test
    void lifecycleCallbacksSetTimestamps() throws InterruptedException {
        Project project = new Project();
        project.onCreate();

        assertNotNull(project.getCreatedAt());
        assertNotNull(project.getUpdatedAt());
        LocalDateTime created = project.getCreatedAt();
        LocalDateTime firstUpdated = project.getUpdatedAt();

        Thread.sleep(2);
        project.onUpdate();

        assertEquals(created, project.getCreatedAt());
        assertTrue(project.getUpdatedAt().isAfter(firstUpdated) || project.getUpdatedAt().isEqual(firstUpdated));
    }

    @Test
    void enumLabelsAreAvailable() {
        assertEquals("CI/CD", ProjectCategory.CICD.getLabel());
        assertEquals("Kubernetes", ProjectCategory.KUBERNETES.getLabel());
        assertEquals("Cloud", ProjectCategory.CLOUD.getLabel());
        assertEquals("Terraform", ProjectCategory.TERRAFORM.getLabel());
        assertEquals("DevSecOps", ProjectCategory.DEVSECOPS.getLabel());
        assertEquals("Observability", ProjectCategory.OBSERVABILITY.getLabel());
        assertEquals("Automation", ProjectCategory.AUTOMATION.getLabel());
        assertEquals("Platform Engineering", ProjectCategory.PLATFORM.getLabel());
        assertEquals("Other", ProjectCategory.OTHER.getLabel());

        assertEquals("DEV", ProjectEnvironment.DEV.getLabel());
        assertEquals("QA", ProjectEnvironment.QA.getLabel());
        assertEquals("PPD", ProjectEnvironment.PPD.getLabel());
        assertEquals("PROD", ProjectEnvironment.PROD.getLabel());
        assertEquals("Multi-env", ProjectEnvironment.MULTI.getLabel());

        assertEquals("Low", ProjectPriority.LOW.getLabel());
        assertEquals("Medium", ProjectPriority.MEDIUM.getLabel());
        assertEquals("High", ProjectPriority.HIGH.getLabel());
        assertEquals("Critical", ProjectPriority.CRITICAL.getLabel());

        assertEquals("Planned", ProjectStatus.PLANNED.getLabel());
        assertEquals("Active", ProjectStatus.ACTIVE.getLabel());
        assertEquals("Blocked", ProjectStatus.BLOCKED.getLabel());
        assertEquals("Completed", ProjectStatus.COMPLETED.getLabel());
        assertEquals("Archived", ProjectStatus.ARCHIVED.getLabel());
    }
}
