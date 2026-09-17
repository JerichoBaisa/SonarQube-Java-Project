package com.devopsshack.projectops.model;

public enum ProjectPriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private final String label;

    ProjectPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
