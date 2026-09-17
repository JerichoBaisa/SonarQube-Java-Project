package com.devopsshack.projectops.model;

public enum ProjectStatus {
    PLANNED("Planned"),
    ACTIVE("Active"),
    BLOCKED("Blocked"),
    COMPLETED("Completed"),
    ARCHIVED("Archived");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
