package com.example.diskwizard.domain.model;

public class Disk {

    String id;
    private String name;
    private String description;
    private String details;

    public Disk() {}

    public Disk(String name, String description, String details) {
        this.name = name;
        this.description = description;
        this.details = details;
    }

    public Disk(String id, String name, String description, String details) {
        this.id = id;
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
        return this.details;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
