package com.example.diskwizard.domain.model;

public class Disk {

    private final String name;
    private final String description;
    private final String details;

    public Disk(String name, String description, String details) {
        this.name = name;
        this.description = description;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDetails() {
        return details;
    }

}
