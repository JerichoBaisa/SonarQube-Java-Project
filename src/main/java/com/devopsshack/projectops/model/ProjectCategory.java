package com.devopsshack.projectops.model;

public enum ProjectCategory {
    CICD("CI/CD"),
    KUBERNETES("Kubernetes"),
    CLOUD("Cloud"),
    TERRAFORM("Terraform"),
    DEVSECOPS("DevSecOps"),
    OBSERVABILITY("Observability"),
    AUTOMATION("Automation"),
    PLATFORM("Platform Engineering"),
    OTHER("Other");

    private final String label;

    ProjectCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
