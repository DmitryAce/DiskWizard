package com.example.diskwizard.domain.model;

public class Comment {

    String id;
    String diskId;
    String userId;
    private String author;
    private String maintext;
    private String date;

    public Comment() {}

    public Comment(String Author, String maintext, String date) {
        this.author = Author;
        this.maintext = maintext;
        this.date = date;
    }

    public Comment(String id, String diskId, String userId, String Author, String maintext, String date) {
        this.id = id;
        this.diskId = diskId;
        this.userId = userId;
        this.author = Author;
        this.maintext = maintext;
        this.date = date;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getMaintext() {
        return this.maintext;
    }

    public String getDate() {
        return this.date;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDiskId() {
        return diskId;
    }

    public void setDiskId(String diskId) {
        this.diskId = diskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}