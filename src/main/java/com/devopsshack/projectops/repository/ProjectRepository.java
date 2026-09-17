package com.devopsshack.projectops.repository;

import com.devopsshack.projectops.model.Project;
import com.devopsshack.projectops.model.ProjectPriority;
import com.devopsshack.projectops.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    long countByStatus(ProjectStatus status);
    long countByPriority(ProjectPriority priority);
    long countByFavoriteTrue();

    @Query("select coalesce(avg(p.progress), 0) from Project p")
    Double averageProgress();
}
