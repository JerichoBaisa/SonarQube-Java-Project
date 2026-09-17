package com.devopsshack.projectops.model;

public enum ProjectEnvironment {
    DEV("DEV"),
    QA("QA"),
    PPD("PPD"),
    PROD("PROD"),
    MULTI("Multi-env");

    private final String label;

    ProjectEnvironment(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
