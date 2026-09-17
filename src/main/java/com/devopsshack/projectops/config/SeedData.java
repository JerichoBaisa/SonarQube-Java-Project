package com.devopsshack.projectops.config;

import com.devopsshack.projectops.model.*;
import com.devopsshack.projectops.repository.ProjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seedProjects(ProjectRepository repository) {
        return args -> {
            if (repository.count() > 0) return;

            LocalDate today = LocalDate.now();
            repository.saveAll(List.of(
                    new Project("Production CI/CD Pipeline",
                            "Secure build-test-scan-deploy pipeline with release gates and rollback support.",
                            ProjectCategory.CICD, ProjectStatus.ACTIVE, ProjectPriority.CRITICAL,
                            ProjectEnvironment.MULTI, "DevOps Shack", 78, true,
                            "https://github.com/jaiswaladi246", "jenkins,github-actions,docker,security",
                            today.minusDays(18), today.plusDays(7)),
                    new Project("Kubernetes Three-Tier Platform",
                            "Frontend, API and database workloads with ingress, autoscaling and persistent storage.",
                            ProjectCategory.KUBERNETES, ProjectStatus.ACTIVE, ProjectPriority.HIGH,
                            ProjectEnvironment.PROD, "Platform Team", 64, true,
                            "https://github.com/jaiswaladi246", "kubernetes,ingress,hpa,pvc",
                            today.minusDays(12), today.plusDays(14)),
                    new Project("Terraform Multi-AZ Foundation",
                            "Reusable Terraform modules for VPC, subnets, NAT, ALB, Auto Scaling and remote state.",
                            ProjectCategory.TERRAFORM, ProjectStatus.PLANNED, ProjectPriority.HIGH,
                            ProjectEnvironment.MULTI, "Cloud Team", 22, false,
                            "https://github.com/jaiswaladi246", "terraform,aws,vpc,modules",
                            today.minusDays(2), today.plusDays(25)),
                    new Project("DevSecOps Security Gates",
                            "Integrate SAST, SCA, container and IaC scanning into the delivery workflow.",
                            ProjectCategory.DEVSECOPS, ProjectStatus.BLOCKED, ProjectPriority.CRITICAL,
                            ProjectEnvironment.QA, "Security Team", 49, true,
                            "https://github.com/jaiswaladi246", "sonarqube,trivy,sast,sca,iac",
                            today.minusDays(10), today.plusDays(10)),
                    new Project("Observability Command Center",
                            "Unified metrics, logs and alerting dashboard for application and Kubernetes health.",
                            ProjectCategory.OBSERVABILITY, ProjectStatus.COMPLETED, ProjectPriority.MEDIUM,
                            ProjectEnvironment.PROD, "SRE Team", 100, false,
                            "https://github.com/jaiswaladi246", "prometheus,grafana,loki,alerts",
                            today.minusDays(30), today.minusDays(1)),
                    new Project("Server Automation Toolkit",
                            "Reusable operational scripts for provisioning, validation, backup and self-healing.",
                            ProjectCategory.AUTOMATION, ProjectStatus.PLANNED, ProjectPriority.MEDIUM,
                            ProjectEnvironment.DEV, "DevOps Shack", 12, false,
                            "https://github.com/jaiswaladi246", "linux,shell,automation,systemd",
                            today, today.plusDays(30))
            ));
        };
    }
}
